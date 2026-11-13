const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const db = require('../config/store');
const { JWT_SECRET } = require('../middleware/auth');

// Email regex pattern
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function generateOtp() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// 1. Register User
exports.register = async (req, res) => {
  try {
    const { fullName, email, password, confirmPassword } = req.body;

    // Validations
    if (!fullName || fullName.trim().length < 2) {
      return res.status(400).json({
        success: false,
        message: 'Full Name must be at least 2 characters.'
      });
    }

    if (!email || !EMAIL_REGEX.test(email.trim())) {
      return res.status(400).json({
        success: false,
        message: 'Please provide a valid email address.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();

    // Check if email already registered and verified
    const existing = await db.users.findOne({ email: normalizedEmail });
    if (existing && existing.isEmailVerified) {
      return res.status(409).json({
        success: false,
        message: 'An account with this email address already exists. Please log in.'
      });
    }

    if (!password || password.length < 8) {
      return res.status(400).json({
        success: false,
        message: 'Password must be at least 8 characters.'
      });
    }

    if (password !== confirmPassword) {
      return res.status(400).json({
        success: false,
        message: 'Passwords do not match.'
      });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    // Generate 6-digit OTP with 10 min expiry
    const otp = generateOtp();
    const otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();

    let user;
    if (existing && !existing.isEmailVerified) {
      // Update unverified user record with new password & OTP
      await db.users.updateOne(
        { email: normalizedEmail },
        {
          fullName: fullName.trim(),
          passwordHash,
          verificationOtp: otp,
          otpExpiresAt,
          updatedAt: new Date().toISOString()
        }
      );
      user = await db.users.findOne({ email: normalizedEmail });
    } else {
      user = await db.users.create({
        fullName: fullName.trim(),
        email: normalizedEmail,
        passwordHash,
        isEmailVerified: false,
        verificationOtp: otp,
        otpExpiresAt,
        profilePicture: '',
        targetExaminations: ['UPSC'],
        preferredCategories: ['National Affairs', 'Economy', 'Science and Technology'],
        difficultyLevel: 'Medium',
        dailyQuestionTarget: 20,
        createdAt: new Date().toISOString(),
        lastLoginAt: null
      });
    }

    console.log(`[CAP AI Auth] Verification OTP for ${normalizedEmail}: ${otp}`);

    res.status(201).json({
      success: true,
      message: 'Account registered successfully. Please verify your email with the OTP sent.',
      email: normalizedEmail,
      debugOtp: otp // Provides seamless verification during testing
    });
  } catch (err) {
    console.error('Registration error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error during registration. Please try again later.'
    });
  }
};

// 2. Verify Email via OTP
exports.verifyEmail = async (req, res) => {
  try {
    const { email, otp } = req.body;

    if (!email || !otp) {
      return res.status(400).json({
        success: false,
        message: 'Email and 6-digit OTP are required.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await db.users.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'No account found with this email.'
      });
    }

    if (user.isEmailVerified) {
      const token = jwt.sign({ userId: user._id, email: user.email }, JWT_SECRET, { expiresIn: '30d' });
      return res.status(200).json({
        success: true,
        message: 'Email already verified.',
        token,
        user: sanitizeUser(user)
      });
    }

    // Verify OTP and check expiration
    if (user.verificationOtp !== otp.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Invalid OTP. Please check the code and try again.'
      });
    }

    if (new Date(user.otpExpiresAt) < new Date()) {
      return res.status(400).json({
        success: false,
        message: 'OTP has expired. Please tap "Resend OTP" to receive a new code.'
      });
    }

    // Mark email as verified
    await db.users.updateOne(
      { _id: user._id },
      {
        isEmailVerified: true,
        verificationOtp: null,
        otpExpiresAt: null,
        lastLoginAt: new Date().toISOString()
      }
    );

    const updatedUser = await db.users.findById(user._id);
    const token = jwt.sign({ userId: updatedUser._id, email: updatedUser.email }, JWT_SECRET, { expiresIn: '30d' });

    res.status(200).json({
      success: true,
      message: 'Email verified successfully! Welcome to CAP AI.',
      token,
      user: sanitizeUser(updatedUser)
    });
  } catch (err) {
    console.error('Verify email error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error during verification. Please try again.'
    });
  }
};

// 3. Resend OTP
exports.resendOtp = async (req, res) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Email is required.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await db.users.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'No account found with this email.'
      });
    }

    const newOtp = generateOtp();
    const otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();

    await db.users.updateOne(
      { _id: user._id },
      {
        verificationOtp: newOtp,
        otpExpiresAt
      }
    );

    console.log(`[CAP AI Auth] Resent OTP for ${normalizedEmail}: ${newOtp}`);

    res.status(200).json({
      success: true,
      message: 'A new 6-digit OTP has been sent to your email.',
      email: normalizedEmail,
      debugOtp: newOtp
    });
  } catch (err) {
    console.error('Resend OTP error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error while resending OTP.'
    });
  }
};

// 4. Login
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await db.users.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password. Please try again.'
      });
    }

    // Check password
    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password. Please try again.'
      });
    }

    // Check email verification status
    if (!user.isEmailVerified) {
      // Generate a fresh OTP for verification
      const otp = generateOtp();
      const otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();
      await db.users.updateOne({ _id: user._id }, { verificationOtp: otp, otpExpiresAt });

      console.log(`[CAP AI Auth] Verification OTP for unverified login ${normalizedEmail}: ${otp}`);

      return res.status(403).json({
        success: false,
        isEmailVerified: false,
        message: 'Your email is not verified yet. We have sent an OTP to complete verification.',
        email: normalizedEmail,
        debugOtp: otp
      });
    }

    // Update lastLoginAt
    await db.users.updateOne(
      { _id: user._id },
      { lastLoginAt: new Date().toISOString() }
    );

    const token = jwt.sign({ userId: user._id, email: user.email }, JWT_SECRET, { expiresIn: '30d' });

    res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: sanitizeUser(user)
    });
  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error during login. Please try again.'
    });
  }
};

// 5. Forgot Password
exports.forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;

    if (!email || !EMAIL_REGEX.test(email.trim())) {
      return res.status(400).json({
        success: false,
        message: 'Please enter a valid registered email address.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await db.users.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'No account registered with this email address.'
      });
    }

    const resetOtp = generateOtp();
    const resetExpiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();

    await db.users.updateOne(
      { _id: user._id },
      {
        resetOtp,
        resetExpiresAt
      }
    );

    console.log(`[CAP AI Auth] Password Reset OTP for ${normalizedEmail}: ${resetOtp}`);

    res.status(200).json({
      success: true,
      message: 'Password reset OTP has been sent to your email.',
      email: normalizedEmail,
      debugOtp: resetOtp
    });
  } catch (err) {
    console.error('Forgot password error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error while sending reset OTP.'
    });
  }
};

// 6. Reset Password
exports.resetPassword = async (req, res) => {
  try {
    const { email, otp, newPassword, confirmNewPassword } = req.body;

    if (!email || !otp || !newPassword) {
      return res.status(400).json({
        success: false,
        message: 'Email, OTP, and new password are required.'
      });
    }

    if (newPassword.length < 8) {
      return res.status(400).json({
        success: false,
        message: 'New password must be at least 8 characters long.'
      });
    }

    if (confirmNewPassword && newPassword !== confirmNewPassword) {
      return res.status(400).json({
        success: false,
        message: 'Passwords do not match.'
      });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await db.users.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User account not found.'
      });
    }

    if (user.resetOtp !== otp.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Invalid password reset OTP.'
      });
    }

    if (new Date(user.resetExpiresAt) < new Date()) {
      return res.status(400).json({
        success: false,
        message: 'Password reset OTP has expired. Please request a new code.'
      });
    }

    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(newPassword, salt);

    await db.users.updateOne(
      { _id: user._id },
      {
        passwordHash,
        resetOtp: null,
        resetExpiresAt: null,
        isEmailVerified: true,
        updatedAt: new Date().toISOString()
      }
    );

    res.status(200).json({
      success: true,
      message: 'Password updated successfully! Please log in with your new password.'
    });
  } catch (err) {
    console.error('Reset password error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error during password reset.'
    });
  }
};

// 7. Logout
exports.logout = async (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Logged out successfully.'
  });
};

// 8. Get Me (Protected)
exports.getMe = async (req, res) => {
  res.status(200).json({
    success: true,
    user: sanitizeUser(req.user)
  });
};

// 9. Update Profile (Protected)
exports.updateProfile = async (req, res) => {
  try {
    const {
      fullName,
      profilePicture,
      targetExaminations,
      preferredCategories,
      difficultyLevel,
      dailyQuestionTarget
    } = req.body;

    const updates = {};
    if (fullName) updates.fullName = fullName.trim();
    if (profilePicture !== undefined) updates.profilePicture = profilePicture;
    if (Array.isArray(targetExaminations)) updates.targetExaminations = targetExaminations;
    if (Array.isArray(preferredCategories)) updates.preferredCategories = preferredCategories;
    if (difficultyLevel) updates.difficultyLevel = difficultyLevel;
    if (dailyQuestionTarget !== undefined) updates.dailyQuestionTarget = Number(dailyQuestionTarget);

    await db.users.updateOne({ _id: req.user._id }, updates);
    const updated = await db.users.findById(req.user._id);

    res.status(200).json({
      success: true,
      message: 'Profile updated successfully.',
      user: sanitizeUser(updated)
    });
  } catch (err) {
    console.error('Update profile error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error updating profile.'
    });
  }
};

// 10. Delete Account (Protected)
exports.deleteAccount = async (req, res) => {
  try {
    const userId = req.user._id;

    // Remove user and all isolated private records
    await db.users.deleteOne({ _id: userId });
    await db.quizAttempts.deleteMany({ userId });
    await db.bookmarks.deleteMany({ userId });
    await db.readingHistory.deleteMany({ userId });
    await db.topicPerformance.deleteMany({ userId });

    res.status(200).json({
      success: true,
      message: 'Your account and all associated preparation data have been deleted permanently.'
    });
  } catch (err) {
    console.error('Delete account error:', err);
    res.status(500).json({
      success: false,
      message: 'Server error deleting account.'
    });
  }
};

function sanitizeUser(user) {
  const { passwordHash, verificationOtp, otpExpiresAt, resetOtp, resetExpiresAt, ...safe } = user;
  return safe;
}
