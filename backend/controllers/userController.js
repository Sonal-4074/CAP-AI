const db = require('../config/store');

// Get personalized dashboard data for the authenticated user
exports.getPreparationData = async (req, res) => {
  try {
    const userId = req.user._id;

    // Fetch user-isolated records
    const quizAttempts = (await db.quizAttempts.find({ userId }))
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const topicPerf = await db.topicPerformance.find({ userId });
    const bookmarks = await db.bookmarks.find({ userId });
    const history = await db.readingHistory.find({ userId });

    // Derive analytics
    const totalAttempted = quizAttempts.reduce((sum, q) => sum + (q.totalQuestions || 0), 0);
    const totalCorrect = quizAttempts.reduce((sum, q) => sum + (q.score || 0), 0);
    const overallAccuracy = totalAttempted > 0 ? (totalCorrect / totalAttempted) * 100 : 0.0;

    // Detect weak topics (< 50% accuracy or marked Weak)
    const weakTopics = topicPerf
      .filter(t => t.accuracy < 50 || t.masteryLevel === 'Weak')
      .map(t => t.topic);

    // Default mock recommendations if none yet
    const revisionRecommendations = weakTopics.length > 0
      ? weakTopics.slice(0, 3).map(t => ({
          topic: t,
          reason: 'Accuracy below 50% in recent tests',
          priority: 'High'
        }))
      : [
          { topic: 'Monetary Policy & RBI', reason: 'High yield for current exams', priority: 'Medium' },
          { topic: 'Constitutional Provisions', reason: 'High question frequency', priority: 'Medium' }
        ];

    res.status(200).json({
      success: true,
      data: {
        userId,
        fullName: req.user.fullName,
        email: req.user.email,
        targetExaminations: req.user.targetExaminations || ['UPSC'],
        dailyQuestionTarget: req.user.dailyQuestionTarget || 20,
        difficultyLevel: req.user.difficultyLevel || 'Medium',
        totalAttempted,
        totalCorrect,
        overallAccuracy: Math.round(overallAccuracy * 10) / 10,
        quizHistory: quizAttempts.slice(0, 10),
        topicPerformance: topicPerf,
        weakTopics,
        revisionRecommendations,
        bookmarksCount: bookmarks.length,
        articlesReadCount: history.length
      }
    });
  } catch (err) {
    console.error('Error fetching preparation data:', err);
    res.status(500).json({
      success: false,
      message: 'Failed to load personalized preparation data.'
    });
  }
};

// Record a quiz attempt and update topic mastery for this authenticated user
exports.recordQuizAttempt = async (req, res) => {
  try {
    const userId = req.user._id;
    const { topic, score, totalQuestions, answers, examType } = req.body;

    const accuracy = totalQuestions > 0 ? (score / totalQuestions) * 100 : 0;

    const attempt = await db.quizAttempts.create({
      userId,
      topic: topic || 'General Current Affairs',
      score: Number(score) || 0,
      totalQuestions: Number(totalQuestions) || 0,
      accuracy: Math.round(accuracy * 10) / 10,
      examType: examType || 'General',
      answers: answers || []
    });

    // Update topic performance
    let masteryLevel = 'Weak';
    if (accuracy >= 85) masteryLevel = 'Strong';
    else if (accuracy >= 70) masteryLevel = 'Good';
    else if (accuracy >= 50) masteryLevel = 'Needs Improvement';

    const existingPerf = await db.topicPerformance.findOne({ userId, topic });
    if (existingPerf) {
      const newAttempted = (existingPerf.attempted || 0) + totalQuestions;
      const newCorrect = (existingPerf.correct || 0) + score;
      const newAccuracy = newAttempted > 0 ? (newCorrect / newAttempted) * 100 : 0;

      let newMastery = 'Weak';
      if (newAccuracy >= 85) newMastery = 'Strong';
      else if (newAccuracy >= 70) newMastery = 'Good';
      else if (newAccuracy >= 50) newMastery = 'Needs Improvement';

      await db.topicPerformance.updateOne(
        { _id: existingPerf._id },
        {
          attempted: newAttempted,
          correct: newCorrect,
          accuracy: Math.round(newAccuracy * 10) / 10,
          masteryLevel: newMastery
        }
      );
    } else {
      await db.topicPerformance.create({
        userId,
        topic: topic || 'General Current Affairs',
        attempted: totalQuestions,
        correct: score,
        accuracy: Math.round(accuracy * 10) / 10,
        masteryLevel
      });
    }

    res.status(201).json({
      success: true,
      message: 'Quiz attempt recorded successfully.',
      attempt
    });
  } catch (err) {
    console.error('Error recording quiz attempt:', err);
    res.status(500).json({
      success: false,
      message: 'Failed to record quiz attempt.'
    });
  }
};

// Toggle user bookmark
exports.toggleBookmark = async (req, res) => {
  try {
    const userId = req.user._id;
    const { articleId, title, category } = req.body;

    const existing = await db.bookmarks.findOne({ userId, articleId });
    if (existing) {
      await db.bookmarks.deleteOne({ _id: existing._id });
      return res.status(200).json({
        success: true,
        bookmarked: false,
        message: 'Bookmark removed.'
      });
    } else {
      await db.bookmarks.create({
        userId,
        articleId,
        title: title || 'Current Affairs Article',
        category: category || 'National'
      });
      return res.status(201).json({
        success: true,
        bookmarked: true,
        message: 'Article bookmarked.'
      });
    }
  } catch (err) {
    console.error('Bookmark toggle error:', err);
    res.status(500).json({ success: false, message: 'Failed to toggle bookmark.' });
  }
};

// Get bookmarks for authenticated user
exports.getBookmarks = async (req, res) => {
  try {
    const userId = req.user._id;
    const bookmarks = await db.bookmarks.find({ userId });
    res.status(200).json({ success: true, bookmarks });
  } catch (err) {
    console.error('Error fetching bookmarks:', err);
    res.status(500).json({ success: false, message: 'Failed to load bookmarks.' });
  }
};
