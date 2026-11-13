const UserPerformance = require('../models/UserPerformance');
const User = require('../models/User');
const store = require('../config/store');

/**
 * GET /api/performance
 * Full analytical report of user preparation
 */
exports.getPerformance = async (req, res) => {
  try {
    const userId = req.user.userId;
    let perf;

    if (store.isMongoActive()) {
      perf = await UserPerformance.findOne({ userId }).lean();
    } else {
      perf = await store.userPerformances.findOne({ userId });
    }

    if (!perf) {
      perf = {
        userId,
        totalAttempted: 0,
        totalCorrect: 0,
        overallAccuracy: 0.0,
        topicStats: [],
        weakTopics: [],
        strongTopics: [],
        streakDays: 1,
        bookmarksCount: 0
      };
    }

    return res.status(200).json({
      success: true,
      data: perf
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve performance analytics.'
    });
  }
};

/**
 * GET /api/user/preparation-data
 * Exact contract required by Android CapApiService: Response<PreparationDataResponse>
 */
exports.getPreparationData = async (req, res) => {
  try {
    const userId = req.user.userId;
    let user, perf;

    if (store.isMongoActive()) {
      user = await User.findById(userId).lean();
      perf = await UserPerformance.findOne({ userId }).lean();
    } else {
      user = await store.users.findById(userId);
      perf = await store.userPerformances.findOne({ userId });
    }

    const preparationDto = {
      userId,
      fullName: user?.fullName || 'Exam Aspirant',
      email: user?.email || '',
      targetExaminations: user?.targetExaminations || ['UPSC'],
      dailyQuestionTarget: user?.dailyQuestionTarget || 20,
      difficultyLevel: user?.difficultyLevel || 'Medium',
      totalAttempted: perf?.totalAttempted || 0,
      totalCorrect: perf?.totalCorrect || 0,
      overallAccuracy: perf?.overallAccuracy || 0.0,
      weakTopics: perf?.weakTopics || ['Polity - Constitutional Bodies', 'Economy - Monetary Policy'],
      bookmarksCount: perf?.bookmarks ? perf.bookmarks.length : 0,
      articlesReadCount: perf?.articlesReadCount || 12
    };

    return res.status(200).json({
      success: true,
      data: preparationDto
    });
  } catch (error) {
    console.error('getPreparationData error:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve user preparation profile.'
    });
  }
};

/**
 * GET /api/performance/weaknesses
 * Weakness detection API
 */
exports.getWeaknesses = async (req, res) => {
  try {
    const userId = req.user.userId;
    let perf;

    if (store.isMongoActive()) {
      perf = await UserPerformance.findOne({ userId }).lean();
    } else {
      perf = await store.userPerformances.findOne({ userId });
    }

    const weakList = perf?.weakTopics || ['Economy - Base Effect', 'Polity - Sub-Classification'];

    return res.status(200).json({
      success: true,
      weakTopics: weakList,
      recommendation: 'Targeted revision sessions generated via Groq AI are recommended for topics with under 50% accuracy.'
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to detect weaknesses.'
    });
  }
};

/**
 * POST /api/user/bookmarks
 * Toggle bookmarks for articles
 */
exports.toggleBookmark = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { articleId, title, category } = req.body;

    if (!articleId) {
      return res.status(400).json({ success: false, message: 'Article ID is required.' });
    }

    let perf;
    if (store.isMongoActive()) {
      perf = await UserPerformance.findOne({ userId });
    } else {
      perf = await store.userPerformances.findOne({ userId });
    }

    if (!perf) {
      const newPerf = {
        userId,
        totalAttempted: 0,
        totalCorrect: 0,
        overallAccuracy: 0,
        topicStats: [],
        weakTopics: [],
        strongTopics: [],
        bookmarks: [{ articleId, title: title || 'Article', category: category || 'General', savedAt: new Date() }]
      };
      if (store.isMongoActive()) {
        await UserPerformance.create(newPerf);
      } else {
        await store.userPerformances.insertOne(newPerf);
      }
      return res.status(200).json({ success: true, message: 'Article bookmarked.' });
    }

    const bookmarks = perf.bookmarks || [];
    const exists = bookmarks.some(b => b.articleId === articleId);

    if (exists) {
      const filtered = bookmarks.filter(b => b.articleId !== articleId);
      if (store.isMongoActive()) {
        await UserPerformance.updateOne({ userId }, { $set: { bookmarks: filtered } });
      } else {
        await store.userPerformances.updateOne({ userId }, { bookmarks: filtered });
      }
      return res.status(200).json({ success: true, message: 'Bookmark removed.' });
    } else {
      bookmarks.push({ articleId, title: title || 'Article', category: category || 'General', savedAt: new Date() });
      if (store.isMongoActive()) {
        await UserPerformance.updateOne({ userId }, { $set: { bookmarks } });
      } else {
        await store.userPerformances.updateOne({ userId }, { bookmarks });
      }
      return res.status(200).json({ success: true, message: 'Article saved to bookmarks.' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to update bookmark.' });
  }
};
