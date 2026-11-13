const groqService = require('../services/groqService');
const KnowledgeMap = require('../models/KnowledgeMap');
const Question = require('../models/Question');
const store = require('../config/store');

/**
 * POST /api/ai/analyze-news
 * Matches Android CapApiService: Response<AnalysisResponse>
 */
exports.analyzeNews = async (req, res) => {
  try {
    const article = req.body.article || req.body;
    if (!article || (!article.title && !article.content && !article.summary)) {
      return res.status(400).json({
        success: false,
        message: 'Valid article object with title or content is required.'
      });
    }

    const analysis = await groqService.analyzeNews(article);

    return res.status(200).json({
      success: true,
      data: analysis
    });
  } catch (error) {
    console.error('AI analyzeNews error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'AI news analysis failed.'
    });
  }
};

/**
 * POST /api/ai/knowledge-map
 * Matches Android CapApiService: Response<KnowledgeMapResponse>
 */
exports.generateKnowledgeMap = async (req, res) => {
  try {
    const { topic, contextText } = req.body;
    if (!topic) {
      return res.status(400).json({
        success: false,
        message: 'Topic string is required for Knowledge Map generation.'
      });
    }

    const mapData = await groqService.generateKnowledgeMap(topic, contextText);

    // Save to database/store if user is logged in
    const userId = req.user?.userId;
    if (userId) {
      const mapDoc = {
        topic,
        userId,
        mainTopic: mapData.mainTopic,
        nodes: mapData.nodes,
        relationships: mapData.relationships,
        createdAt: new Date()
      };
      if (store.isMongoActive()) {
        await KnowledgeMap.create(mapDoc);
      } else {
        await store.knowledgeMaps.insertOne(mapDoc);
      }
    }

    return res.status(200).json({
      success: true,
      data: mapData
    });
  } catch (error) {
    console.error('AI knowledge map error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to generate Knowledge Map.'
    });
  }
};

/**
 * POST /api/ai/generate-questions
 * Matches Android CapApiService: Response<QuestionsResponse>
 */
exports.generateQuestions = async (req, res) => {
  try {
    const { articleText, targetExam = 'UPSC', count = 5, difficulty = 'Medium' } = req.body;
    if (!articleText) {
      return res.status(400).json({
        success: false,
        message: 'Article text is required for question generation.'
      });
    }

    const questions = await groqService.generateQuestions(
      articleText,
      targetExam,
      Math.min(Number(count) || 5, 10),
      difficulty
    );

    return res.status(200).json({
      success: true,
      data: questions
    });
  } catch (error) {
    console.error('AI generateQuestions error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to generate questions.'
    });
  }
};

/**
 * POST /api/ai/explain-question
 * Matches Android CapApiService: Response<BaseResponse>
 */
exports.explainQuestion = async (req, res) => {
  try {
    const { question, options, selectedOption, correctOption } = req.body;
    if (!question || !correctOption) {
      return res.status(400).json({
        success: false,
        message: 'Question and correctOption are required.'
      });
    }

    const explanation = await groqService.explainQuestion(
      question,
      options || [],
      selectedOption || 'Not selected',
      correctOption
    );

    return res.status(200).json({
      success: true,
      message: explanation
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to generate explanation.'
    });
  }
};

/**
 * POST /api/ai/revision
 * Matches Android CapApiService: Response<RevisionResponse>
 */
exports.generateRevision = async (req, res) => {
  try {
    const { topic, weakConcepts, previousAccuracy } = req.body;
    if (!topic) {
      return res.status(400).json({
        success: false,
        message: 'Topic is required for revision generation.'
      });
    }

    const revision = await groqService.generateRevision(
      topic,
      weakConcepts || [],
      Number(previousAccuracy) || 45.0
    );

    return res.status(200).json({
      success: true,
      data: revision
    });
  } catch (error) {
    console.error('AI generateRevision error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to generate revision session.'
    });
  }
};
