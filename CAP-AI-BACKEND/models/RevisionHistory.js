const mongoose = require('mongoose');

const revisionHistorySchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true
    },
    topic: {
      type: String,
      required: true
    },
    concepts: {
      type: [String],
      default: []
    },
    explanation: {
      type: String,
      default: ''
    },
    keyFacts: {
      type: [String],
      default: []
    },
    previousAccuracy: {
      type: Number,
      default: 0
    },
    currentAccuracy: {
      type: Number,
      default: 0
    },
    masteryUpdated: {
      type: Boolean,
      default: false
    }
  },
  {
    timestamps: true
  }
);

revisionHistorySchema.index({ userId: 1, createdAt: -1 });

module.exports = mongoose.models.RevisionHistory || mongoose.model('RevisionHistory', revisionHistorySchema);
