const mongoose = require('mongoose');

const currentAffairsSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: [true, 'Article title is required'],
      trim: true
    },
    summary: {
      type: String,
      required: [true, 'Article summary is required']
    },
    content: {
      type: String,
      required: [true, 'Article full content is required']
    },
    category: {
      type: String,
      required: true,
      enum: ['National', 'International', 'Economy', 'Polity', 'Science & Tech', 'Environment', 'Defence', 'General'],
      default: 'National'
    },
    source: {
      type: String,
      default: 'The Hindu / PIB / Indian Express'
    },
    date: {
      type: Date,
      default: Date.now
    },
    examRelevance: {
      type: String,
      default: 'GS Paper II & III'
    },
    keyFacts: {
      type: [String],
      default: []
    },
    tags: {
      type: [String],
      default: []
    },
    imageUrl: {
      type: String,
      default: ''
    }
  },
  {
    timestamps: true
  }
);

currentAffairsSchema.index({ category: 1, date: -1 });
currentAffairsSchema.index({ title: 'text', summary: 'text', content: 'text' });

module.exports = mongoose.models.CurrentAffairs || mongoose.model('CurrentAffairs', currentAffairsSchema);
