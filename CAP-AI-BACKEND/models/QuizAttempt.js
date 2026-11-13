const mongoose = require('mongoose');

const quizAttemptSchema = new mongoose.Schema(
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
    score: {
      type: Number,
      required: true
    },
    totalQuestions: {
      type: Number,
      required: true
    },
    accuracy: {
      type: Number,
      default: function () {
        return this.totalQuestions > 0 ? (this.score / this.totalQuestions) * 100 : 0;
      }
    },
    examType: {
      type: String,
      default: 'General'
    },
    answers: [
      {
        questionId: { type: String },
        selectedOption: { type: Number },
        isCorrect: { type: Boolean }
      }
    ]
  },
  {
    timestamps: true
  }
);

quizAttemptSchema.index({ userId: 1, createdAt: -1 });

module.exports = mongoose.models.QuizAttempt || mongoose.model('QuizAttempt', quizAttemptSchema);
