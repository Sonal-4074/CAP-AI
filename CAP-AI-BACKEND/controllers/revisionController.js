const RevisionHistory = require('../models/RevisionHistory');
const store = require('../config/store');
const groqService = require('../services/groqService');

/**
 * GET /api/revision
 * Returns recent revision history for the user
 */
exports.getRevisionList = async (req, res) => {
  try {
    const userId = req.user.userId;
    let revisions;

    if (store.isMongoActive()) {
      revisions = await RevisionHistory.find({ userId }).sort({ createdAt: -1 }).limit(15).lean();
    } else {
      const all = await store.revisionHistories.find({ userId });
      revisions = all.reverse().slice(0, 15);
    }

    return res.status(200).json({
      success: true,
      data: revisions
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve revision history.'
    });
  }
};

/**
 * POST /api/revision/generate
 * Generates an adaptive revision session using Groq AI and saves to history
 */
exports.createRevisionSession = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { topic, weakConcepts = [], previousAccuracy = 45.0 } = req.body;

    if (!topic) {
      return res.status(400).json({
        success: false,
        message: 'Topic is required to generate a revision session.'
      });
    }

    const sessionData = await groqService.generateRevision(topic, weakConcepts, Number(previousAccuracy));

    const revisionDoc = {
      userId,
      topic,
      concepts: sessionData.concepts || weakConcepts,
      explanation: sessionData.explanation,
      keyFacts: sessionData.keyFacts,
      previousAccuracy: Number(previousAccuracy),
      currentAccuracy: 0,
      masteryUpdated: false,
      createdAt: new Date()
    };

    let saved;
    if (store.isMongoActive()) {
      saved = await RevisionHistory.create(revisionDoc);
    } else {
      saved = await store.revisionHistories.insertOne(revisionDoc);
    }

    return res.status(200).json({
      success: true,
      data: {
        ...sessionData,
        revisionId: saved._id ? saved._id.toString() : saved.id
      }
    });
  } catch (error) {
    console.error('createRevisionSession error:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to create revision session.'
    });
  }
};

/**
 * POST /api/revision/:id/complete
 * Updates revision outcome after the user solves the revision questions
 */
exports.completeRevisionSession = async (req, res) => {
  try {
    const { id } = req.params;
    const { currentAccuracy, masteryUpdated = true } = req.body;

    const updates = {
      currentAccuracy: Number(currentAccuracy) || 0,
      masteryUpdated: !!masteryUpdated
    };

    if (store.isMongoActive()) {
      await RevisionHistory.findByIdAndUpdate(id, { $set: updates });
    } else {
      await store.revisionHistories.updateOne({ _id: id }, updates);
    }

    return res.status(200).json({
      success: true,
      message: 'Revision session marked as completed.'
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to update revision session.'
    });
  }
};
