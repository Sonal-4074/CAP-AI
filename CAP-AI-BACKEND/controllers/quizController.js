const QuizAttempt = require('../models/QuizAttempt');
const UserPerformance = require('../models/UserPerformance');
const store = require('../config/store');
const groqService = require('../services/groqService');

/**
 * POST /api/quiz/submit or POST /api/user/quiz-attempt
 * Records a quiz attempt and automatically updates user performance & weakness profiles.
 */
exports.submitQuiz = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { topic, score, totalQuestions, examType = 'General', answers = [] } = req.body;

    if (!topic || score === undefined || !totalQuestions) {
      return res.status(400).json({
        success: false,
        message: 'Topic, score, and totalQuestions are required.'
      });
    }

    const numericScore = Number(score);
    const numericTotal = Number(totalQuestions);
    const accuracy = numericTotal > 0 ? (numericScore / numericTotal) * 100 : 0;

    const attemptDoc = {
      userId,
      topic,
      score: numericScore,
      totalQuestions: numericTotal,
      accuracy,
      examType,
      answers,
      createdAt: new Date()
    };

    if (store.isMongoActive()) {
      await QuizAttempt.create(attemptDoc);
    } else {
      await store.quizAttempts.insertOne(attemptDoc);
    }

    // Update User Performance Metrics
    await updateUserPerformance(userId, topic, numericScore, numericTotal);

    return res.status(200).json({
      success: true,
      message: 'Quiz attempt recorded and performance metrics updated successfully.',
      accuracy: Math.round(accuracy * 10) / 10
    });
  } catch (error) {
    console.error('Quiz submit error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to record quiz attempt.'
    });
  }
};

/**
 * GET /api/quiz/history
 * Returns the authenticated user's past quiz submissions.
 */
exports.getQuizHistory = async (req, res) => {
  try {
    const userId = req.user.userId;
    let attempts;

    if (store.isMongoActive()) {
      attempts = await QuizAttempt.find({ userId }).sort({ createdAt: -1 }).limit(20).lean();
    } else {
      const all = await store.quizAttempts.find({ userId });
      attempts = all.reverse().slice(0, 20);
    }

    return res.status(200).json({
      success: true,
      data: attempts
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve quiz history.'
    });
  }
};

/**
 * Helper to update cumulative performance and detect weaknesses
 */
async function updateUserPerformance(userId, topic, score, total) {
  let perf;
  if (store.isMongoActive()) {
    perf = await UserPerformance.findOne({ userId });
  } else {
    perf = await store.userPerformances.findOne({ userId });
  }

  if (!perf) {
    perf = {
      userId,
      totalAttempted: total,
      totalCorrect: score,
      overallAccuracy: total > 0 ? (score / total) * 100 : 0,
      topicStats: [
        {
          topic,
          attempted: total,
          correct: score,
          accuracy: total > 0 ? (score / total) * 100 : 0,
          masteryLevel: score / total >= 0.75 ? 'Strong' : score / total < 0.5 ? 'Need Focus' : 'Moderate'
        }
      ],
      weakTopics: (score / total) < 0.5 ? [topic] : [],
      strongTopics: (score / total) >= 0.75 ? [topic] : [],
      streakDays: 1,
      lastActiveDate: new Date(),
      bookmarks: [],
      articlesReadCount: 1
    };

    if (store.isMongoActive()) {
      await UserPerformance.create(perf);
    } else {
      await store.userPerformances.insertOne(perf);
    }
    return;
  }

  // Update existing stats
  const newAttempted = (perf.totalAttempted || 0) + total;
  const newCorrect = (perf.totalCorrect || 0) + score;
  const overallAcc = newAttempted > 0 ? (newCorrect / newAttempted) * 100 : 0;

  const topicList = perf.topicStats || [];
  const existingTopicIdx = topicList.findIndex(t => t.topic.toLowerCase() === topic.toLowerCase());

  if (existingTopicIdx !== -1) {
    const t = topicList[existingTopicIdx];
    t.attempted += total;
    t.correct += score;
    t.accuracy = t.attempted > 0 ? (t.correct / t.attempted) * 100 : 0;
    t.masteryLevel = t.accuracy >= 75 ? 'Strong' : t.accuracy < 50 ? 'Need Focus' : 'Moderate';
    topicList[existingTopicIdx] = t;
  } else {
    topicList.push({
      topic,
      attempted: total,
      correct: score,
      accuracy: total > 0 ? (score / total) * 100 : 0,
      masteryLevel: score / total >= 0.75 ? 'Strong' : score / total < 0.5 ? 'Need Focus' : 'Moderate'
    });
  }

  // Recalculate weak and strong topics
  const weakTopics = topicList.filter(t => t.accuracy < 50).map(t => t.topic);
  const strongTopics = topicList.filter(t => t.accuracy >= 75).map(t => t.topic);

  const updates = {
    totalAttempted: newAttempted,
    totalCorrect: newCorrect,
    overallAccuracy: Math.round(overallAcc * 10) / 10,
    topicStats: topicList,
    weakTopics,
    strongTopics,
    lastActiveDate: new Date()
  };

  if (store.isMongoActive()) {
    await UserPerformance.updateOne({ userId }, { $set: updates });
  } else {
    await store.userPerformances.updateOne({ userId }, updates);
  }
}
