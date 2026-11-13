const groqService = require('../services/groqService');

// Analyze news article with Groq
exports.analyzeNews = async (req, res) => {
  try {
    const { article } = req.body;
    if (!article || !article.title) {
      return res.status(400).json({
        success: false,
        message: 'Article object with title is required.'
      });
    }

    const analysis = await groqService.analyzeNews(article);
    res.status(200).json({
      success: true,
      data: analysis
    });
  } catch (err) {
    console.error('AI analyzeNews error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error processing news analysis via Groq AI.'
    });
  }
};

// Generate Knowledge Map with Groq
exports.generateKnowledgeMap = async (req, res) => {
  try {
    const { topic, contextText } = req.body;
    if (!topic) {
      return res.status(400).json({
        success: false,
        message: 'Topic is required to generate a knowledge map.'
      });
    }

    const mapData = await groqService.generateKnowledgeMap(topic, contextText || '');
    res.status(200).json({
      success: true,
      data: mapData
    });
  } catch (err) {
    console.error('AI generateKnowledgeMap error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error generating knowledge map via Groq AI.'
    });
  }
};

// Generate MCQs with Groq
exports.generateQuestions = async (req, res) => {
  try {
    const { articleText, targetExam, count, difficulty } = req.body;
    if (!articleText) {
      return res.status(400).json({
        success: false,
        message: 'Article text is required.'
      });
    }

    const questions = await groqService.generateQuestions(
      articleText,
      targetExam || 'UPSC',
      Number(count) || 5,
      difficulty || 'Medium'
    );

    res.status(200).json({
      success: true,
      data: questions
    });
  } catch (err) {
    console.error('AI generateQuestions error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error generating questions via Groq AI.'
    });
  }
};

// Explain specific question with Groq
exports.explainQuestion = async (req, res) => {
  try {
    const { question, options, selectedOption, correctOption } = req.body;
    if (!question || !options) {
      return res.status(400).json({
        success: false,
        message: 'Question and options are required.'
      });
    }

    const explanation = await groqService.explainQuestion(
      question,
      options,
      selectedOption || '',
      correctOption || ''
    );

    res.status(200).json({
      success: true,
      data: explanation
    });
  } catch (err) {
    console.error('AI explainQuestion error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error explaining question via Groq AI.'
    });
  }
};

// Performance & Weakness analysis
exports.analyzePerformance = async (req, res) => {
  try {
    const { quizHistory, topicStats } = req.body;
    const analysis = await groqService.analyzePerformance(
      quizHistory || [],
      topicStats || []
    );

    res.status(200).json({
      success: true,
      data: analysis
    });
  } catch (err) {
    console.error('AI analyzePerformance error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error analyzing performance via Groq AI.'
    });
  }
};

// Personalized Revision Generation
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
      Number(previousAccuracy) || 45
    );

    res.status(200).json({
      success: true,
      data: revision
    });
  } catch (err) {
    console.error('AI generateRevision error:', err);
    res.status(500).json({
      success: false,
      message: err.message || 'Error generating revision notes via Groq AI.'
    });
  }
};
