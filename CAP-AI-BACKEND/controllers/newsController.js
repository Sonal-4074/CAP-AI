const newsService = require('../services/newsService');
const CurrentAffairs = require('../models/CurrentAffairs');
const store = require('../config/store');

/**
 * GET /api/news
 * Query params: category, search, page, limit
 */
exports.getArticles = async (req, res) => {
  try {
    const { category, search, page = 1, limit = 10 } = req.query;
    const result = await newsService.getArticles({ category, search, page, limit });

    return res.status(200).json({
      success: true,
      data: result.articles,
      pagination: {
        total: result.total,
        page: result.page,
        pages: result.pages
      }
    });
  } catch (error) {
    console.error('getArticles controller error:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve current affairs articles.'
    });
  }
};

/**
 * GET /api/news/:id
 */
exports.getArticleById = async (req, res) => {
  try {
    const { id } = req.params;
    const article = await newsService.getArticleById(id);

    if (!article) {
      return res.status(404).json({
        success: false,
        message: 'Article not found.'
      });
    }

    return res.status(200).json({
      success: true,
      data: article
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve article details.'
    });
  }
};

/**
 * POST /api/news
 * Create new current affairs article
 */
exports.createArticle = async (req, res) => {
  try {
    const { title, summary, content, category, source, examRelevance, keyFacts, tags, imageUrl } = req.body;

    if (!title || !summary || !content) {
      return res.status(400).json({
        success: false,
        message: 'Title, summary, and content are required.'
      });
    }

    const articleData = {
      title,
      summary,
      content,
      category: category || 'General',
      source: source || 'PIB / Hindu',
      examRelevance: examRelevance || 'GS Paper II & III',
      keyFacts: keyFacts || [],
      tags: tags || [],
      imageUrl: imageUrl || '',
      date: new Date()
    };

    let saved;
    if (store.isMongoActive()) {
      saved = await CurrentAffairs.create(articleData);
    } else {
      saved = await store.currentAffairs.insertOne(articleData);
    }

    return res.status(201).json({
      success: true,
      message: 'Article published successfully.',
      data: saved
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to publish article.'
    });
  }
};
