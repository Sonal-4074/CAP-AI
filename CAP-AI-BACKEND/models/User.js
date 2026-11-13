const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema(
  {
    fullName: {
      type: String,
      required: [true, 'Full name is required'],
      trim: true,
      maxlength: [100, 'Full name cannot exceed 100 characters']
    },
    email: {
      type: String,
      required: [true, 'Email address is required'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [
        /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/,
        'Please enter a valid email address'
      ]
    },
    password: {
      type: String,
      required: [true, 'Password is required'],
      minlength: [6, 'Password must be at least 6 characters']
    },
    isEmailVerified: {
      type: Boolean,
      default: false
    },
    otp: {
      code: { type: String, default: null },
      expiresAt: { type: Date, default: null }
    },
    resetPasswordOtp: {
      code: { type: String, default: null },
      expiresAt: { type: Date, default: null }
    },
    profilePicture: {
      type: String,
      default: ''
    },
    targetExaminations: {
      type: [String],
      default: ['UPSC']
    },
    preferredCategories: {
      type: [String],
      default: ['National', 'International', 'Economy', 'Polity', 'Science & Tech']
    },
    difficultyLevel: {
      type: String,
      enum: ['Easy', 'Medium', 'Hard'],
      default: 'Medium'
    },
    dailyQuestionTarget: {
      type: Number,
      default: 20
    }
  },
  {
    timestamps: true
  }
);

// Method to verify password
userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

// Safe JSON serialization without sensitive fields
userSchema.methods.toSafeObject = function () {
  return {
    _id: this._id ? this._id.toString() : this.id,
    fullName: this.fullName,
    email: this.email,
    isEmailVerified: this.isEmailVerified,
    profilePicture: this.profilePicture,
    targetExaminations: this.targetExaminations,
    preferredCategories: this.preferredCategories,
    difficultyLevel: this.difficultyLevel,
    dailyQuestionTarget: this.dailyQuestionTarget,
    createdAt: this.createdAt
  };
};

module.exports = mongoose.models.User || mongoose.model('User', userSchema);
