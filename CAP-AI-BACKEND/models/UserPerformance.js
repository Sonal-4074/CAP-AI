const mongoose = require('mongoose');

const topicStatsSchema = new mongoose.Schema({
  topic: { type: String, required: true },
  attempted: { type: Number, default: 0 },
  correct: { type: Number, default: 0 },
  accuracy: { type: Number, default: 0 },
  masteryLevel: {
    type: String,
    enum: ['Need Focus', 'Moderate', 'Strong'],
    default: 'Moderate'
  }
});

const bookmarkItemSchema = new mongoose.Schema({
  articleId: { type: String, required: true },
  title: { type: String, required: true },
  category: { type: String, default: 'General' },
  savedAt: { type: Date, default: Date.now }
});

const userPerformanceSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      unique: true
    },
    totalAttempted: {
      type: Number,
      default: 0
    },
    totalCorrect: {
      type: Number,
      default: 0
    },
    overallAccuracy: {
      type: Number,
      default: 0.0
    },
    topicStats: [topicStatsSchema],
    weakTopics: {
      type: [String],
      default: []
    },
    strongTopics: {
      type: [String],
      default: []
    },
    streakDays: {
      type: Number,
      default: 1
    },
    lastActiveDate: {
      type: Date,
      default: Date.now
    },
    bookmarks: [bookmarkItemSchema],
    articlesReadCount: {
      type: Number,
      default: 0
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.models.UserPerformance || mongoose.model('UserPerformance', userPerformanceSchema);
