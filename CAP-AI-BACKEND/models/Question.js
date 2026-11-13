const mongoose = require('mongoose');

const questionSchema = new mongoose.Schema(
  {
    question: {
      type: String,
      required: [true, 'Question text is required']
    },
    options: {
      type: [String],
      required: [true, 'Options are required'],
      validate: [val => val.length === 4, 'Must provide exactly 4 options']
    },
    correctIndex: {
      type: Number,
      required: [true, 'Correct index is required'],
      min: 0,
      max: 3
    },
    explanation: {
      type: String,
      required: [true, 'Detailed explanation is required']
    },
    difficulty: {
      type: String,
      enum: ['Easy', 'Medium', 'Hard'],
      default: 'Medium'
    },
    category: {
      type: String,
      required: true,
      default: 'General'
    },
    relatedConcept: {
      type: String,
      default: ''
    },
    examType: {
      type: String,
      default: 'UPSC'
    },
    articleId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'CurrentAffairs',
      default: null
    },
    source: {
      type: String,
      default: 'Groq AI Generated'
    }
  },
  {
    timestamps: true
  }
);

questionSchema.index({ category: 1, difficulty: 1, examType: 1 });

module.exports = mongoose.models.Question || mongoose.model('Question', questionSchema);
