const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const User = require('../models/User');
const store = require('../config/store');
const emailService = require('../services/emailService');
const { JWT_SECRET } = require('../middleware/authMiddleware');

function generateOtp() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

function generateToken(user) {
  const id = user._id ? user._id.toString() : user.id;
  return jwt.sign(
    { id, userId: id, email: user.email },
    JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '30d' }
  );
}

function sanitizeUser(user) {
  return {
    _id: user._id ? user._id.toString() : user.id,
    fullName: user.fullName || '',
    email: user.email || '',
    isEmailVerified: !!user.isEmailVerified,
    profilePicture: user.profilePicture || '',
    targetExaminations: user.targetExaminations && user.targetExaminations.length > 0 ? user.targetExaminations : ['UPSC'],
    preferredCategories: user.preferredCategories && user.preferredCategories.length > 0 ? user.preferredCategories : ['National', 'Economy'],
    difficultyLevel: user.difficultyLevel || 'Medium',
    dailyQuestionTarget: user.dailyQuestionTarget || 20
  };
}

/**
 * 1. Register User
 */
exports.register = async (req, res) => {
  try {
    const { fullName, email, password, confirmPassword } = req.body;

    if (!fullName || !email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Full name, email, and password are required.'
      });
    }

    if (confirmPassword && password !== confirmPassword) {
      return res.status(400).json({
        success: false,
        message: 'Passwords do not match.'
      });
    }

    const cleanEmail = email.toLowerCase().trim();

    // Check if user already exists
    let existingUser;
    if (store.isMongoActive()) {
      existingUser = await User.findOne({ email: cleanEmail });
    } else {
      existingUser = await store.users.findOne({ email: cleanEmail });
    }

    if (existingUser) {
      if (existingUser.isEmailVerified) {
        return res.status(400).json({
          success: false,
          message: 'An account with this email already exists. Please log in.'
        });
      }
      // If user exists but is not verified, generate a new OTP
      const otp = generateOtp();
      const expiresAt = new Date(Date.now() + 15 * 60 * 1000);
      const hashedPassword = await bcrypt.hash(password, 10);

      if (store.isMongoActive()) {
        existingUser.fullName = fullName.trim();
        existingUser.password = hashedPassword;
        existingUser.otp = { code: otp, expiresAt };
        await existingUser.save();
      } else {
        await store.users.updateOne(
          { email: cleanEmail },
          { fullName: fullName.trim(), password: hashedPassword, otp: { code: otp, expiresAt } }
        );
      }

      await emailService.sendVerificationOtp(cleanEmail, otp, fullName.trim());

      return res.status(200).json({
        success: true,
        message: 'Verification OTP has been resent to your email.',
        email: cleanEmail,
        debugOtp: otp
      });
    }

    // Hash password & generate OTP
    const hashedPassword = await bcrypt.hash(password, 10);
    const otp = generateOtp();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    const newUserDoc = {
      fullName: fullName.trim(),
      email: cleanEmail,
      password: hashedPassword,
      isEmailVerified: false,
      otp: { code: otp, expiresAt },
      targetExaminations: ['UPSC'],
      preferredCategories: ['National', 'International', 'Economy', 'Science & Tech'],
      difficultyLevel: 'Medium',
      dailyQuestionTarget: 20
    };

    if (store.isMongoActive()) {
      await User.create(newUserDoc);
    } else {
      await store.users.insertOne(newUserDoc);
    }

    await emailService.sendVerificationOtp(cleanEmail, otp, fullName.trim());

    return res.status(201).json({
      success: true,
      message: 'Account registered successfully. Please verify the 6-digit OTP sent to your email.',
      email: cleanEmail,
      debugOtp: otp
    });
  } catch (error) {
    console.error('Registration error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error during registration.'
    });
  }
};

/**
 * 2. Verify Email OTP
 */
exports.verifyEmail = async (req, res) => {
  try {
    const { email, otp } = req.body;

    if (!email || !otp) {
      return res.status(400).json({
        success: false,
        message: 'Email and 6-digit OTP are required.'
      });
    }

    const cleanEmail = email.toLowerCase().trim();
    let user;

    if (store.isMongoActive()) {
      user = await User.findOne({ email: cleanEmail });
    } else {
      user = await store.users.findOne({ email: cleanEmail });
    }

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'Account not found. Please register first.'
      });
    }

    // Check OTP
    const storedOtp = user.otp?.code;
    const expiresAt = user.otp?.expiresAt ? new Date(user.otp.expiresAt) : null;

    if (!storedOtp || storedOtp !== otp.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Invalid verification OTP code. Please check and try again.'
      });
    }

    if (expiresAt && expiresAt < new Date()) {
      return res.status(400).json({
        success: false,
        message: 'Verification OTP has expired. Please request a new one.'
      });
    }

    // Mark verified
    if (store.isMongoActive()) {
      user.isEmailVerified = true;
      user.otp = { code: null, expiresAt: null };
      await user.save();
    } else {
      user = await store.users.updateOne(
        { email: cleanEmail },
        { isEmailVerified: true, otp: { code: null, expiresAt: null } }
      );
    }

    const token = generateToken(user);
    const safeUser = sanitizeUser(user);

    return res.status(200).json({
      success: true,
      message: 'Email verified successfully. Welcome to CAP AI!',
      token,
      user: safeUser,
      isEmailVerified: true
    });
  } catch (error) {
    console.error('Verify email error:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error during email verification.'
    });
  }
};

/**
 * 3. Resend OTP
 */
exports.resendOtp = async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email address is required.' });
    }

    const cleanEmail = email.toLowerCase().trim();
    let user;
    if (store.isMongoActive()) {
      user = await User.findOne({ email: cleanEmail });
    } else {
      user = await store.users.findOne({ email: cleanEmail });
    }

    if (!user) {
      return res.status(404).json({ success: false, message: 'Account not found.' });
    }

    const otp = generateOtp();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    if (store.isMongoActive()) {
      user.otp = { code: otp, expiresAt };
      await user.save();
    } else {
      await store.users.updateOne({ email: cleanEmail }, { otp: { code: otp, expiresAt } });
    }

    await emailService.sendVerificationOtp(cleanEmail, otp, user.fullName);

    return res.status(200).json({
      success: true,
      message: 'Fresh OTP has been sent to your email.',
      email: cleanEmail,
      debugOtp: otp
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Unable to resend OTP.' });
  }
};

/**
 * 4. User Login
 */
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Please provide both email and password.'
      });
    }

    const cleanEmail = email.toLowerCase().trim();
    let user;

    if (store.isMongoActive()) {
      user = await User.findOne({ email: cleanEmail });
    } else {
      user = await store.users.findOne({ email: cleanEmail });
    }

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password. Please try again.'
      });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password. Please try again.'
      });
    }

    if (!user.isEmailVerified) {
      // Send a fresh OTP to help complete registration
      const otp = generateOtp();
      const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

      if (store.isMongoActive()) {
        user.otp = { code: otp, expiresAt };
        await user.save();
      } else {
        await store.users.updateOne({ email: cleanEmail }, { otp: { code: otp, expiresAt } });
      }

      await emailService.sendVerificationOtp(cleanEmail, otp, user.fullName);

      return res.status(403).json({
        success: false,
        message: 'Your email is not verified yet. A verification OTP has been sent to your inbox.',
        email: cleanEmail,
        debugOtp: otp,
        isEmailVerified: false
      });
    }

    const token = generateToken(user);
    const safeUser = sanitizeUser(user);

    return res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: safeUser,
      isEmailVerified: true
    });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error during login.'
    });
  }
};

/**
 * 5. Forgot Password
 */
exports.forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email address is required.' });
    }

    const cleanEmail = email.toLowerCase().trim();
    let user;
    if (store.isMongoActive()) {
      user = await User.findOne({ email: cleanEmail });
    } else {
      user = await store.users.findOne({ email: cleanEmail });
    }

    if (!user) {
      // For security, return success message even if email is not found
      return res.status(200).json({
        success: true,
        message: 'If an account exists with this email, a reset OTP has been sent.'
      });
    }

    const otp = generateOtp();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    if (store.isMongoActive()) {
      user.resetPasswordOtp = { code: otp, expiresAt };
      await user.save();
    } else {
      await store.users.updateOne({ email: cleanEmail }, { resetPasswordOtp: { code: otp, expiresAt } });
    }

    await emailService.sendPasswordResetOtp(cleanEmail, otp);

    return res.status(200).json({
      success: true,
      message: 'Password reset code has been sent to your email.',
      email: cleanEmail,
      debugOtp: otp
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error during forgot password.' });
  }
};

/**
 * 6. Reset Password
 */
exports.resetPassword = async (req, res) => {
  try {
    const { email, otp, newPassword, confirmNewPassword } = req.body;

    if (!email || !otp || !newPassword) {
      return res.status(400).json({
        success: false,
        message: 'Email, OTP, and new password are required.'
      });
    }

    if (confirmNewPassword && newPassword !== confirmNewPassword) {
      return res.status(400).json({
        success: false,
        message: 'New passwords do not match.'
      });
    }

    const cleanEmail = email.toLowerCase().trim();
    let user;
    if (store.isMongoActive()) {
      user = await User.findOne({ email: cleanEmail });
    } else {
      user = await store.users.findOne({ email: cleanEmail });
    }

    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }

    const storedOtp = user.resetPasswordOtp?.code;
    const expiresAt = user.resetPasswordOtp?.expiresAt ? new Date(user.resetPasswordOtp.expiresAt) : null;

    if (!storedOtp || storedOtp !== otp.trim()) {
      return res.status(400).json({ success: false, message: 'Invalid reset OTP code.' });
    }

    if (expiresAt && expiresAt < new Date()) {
      return res.status(400).json({ success: false, message: 'Reset OTP has expired. Please request a new one.' });
    }

    const hashedPassword = await bcrypt.hash(newPassword, 10);

    if (store.isMongoActive()) {
      user.password = hashedPassword;
      user.resetPasswordOtp = { code: null, expiresAt: null };
      await user.save();
    } else {
      await store.users.updateOne(
        { email: cleanEmail },
        { password: hashedPassword, resetPasswordOtp: { code: null, expiresAt: null } }
      );
    }

    return res.status(200).json({
      success: true,
      message: 'Password has been reset successfully. You can now log in with your new password.'
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error during password reset.' });
  }
};

/**
 * 7. Get Current User Profile (GET /api/auth/me)
 */
exports.getMe = async (req, res) => {
  try {
    const userId = req.user.userId;
    let user;

    if (store.isMongoActive()) {
      user = await User.findById(userId);
    } else {
      user = await store.users.findById(userId);
    }

    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }

    return res.status(200).json({
      success: true,
      user: sanitizeUser(user)
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Error fetching profile.' });
  }
};

/**
 * 8. Update Profile (PUT /api/auth/profile)
 */
exports.updateProfile = async (req, res) => {
  try {
    const userId = req.user.userId;
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
    if (targetExaminations) updates.targetExaminations = targetExaminations;
    if (preferredCategories) updates.preferredCategories = preferredCategories;
    if (difficultyLevel) updates.difficultyLevel = difficultyLevel;
    if (dailyQuestionTarget !== undefined) updates.dailyQuestionTarget = Number(dailyQuestionTarget);

    let updatedUser;
    if (store.isMongoActive()) {
      updatedUser = await User.findByIdAndUpdate(userId, { $set: updates }, { new: true });
    } else {
      updatedUser = await store.users.updateOne({ _id: userId }, updates);
    }

    if (!updatedUser) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }

    return res.status(200).json({
      success: true,
      message: 'Profile updated successfully.',
      user: sanitizeUser(updatedUser)
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Error updating profile.' });
  }
};

/**
 * 9. Logout
 */
exports.logout = async (req, res) => {
  return res.status(200).json({
    success: true,
    message: 'Logged out successfully.'
  });
};

/**
 * 10. Delete Account
 */
exports.deleteAccount = async (req, res) => {
  try {
    const userId = req.user.userId;

    if (store.isMongoActive()) {
      await User.findByIdAndDelete(userId);
    } else {
      await store.users.deleteOne({ _id: userId });
      await store.userPerformances.deleteOne({ userId });
    }

    return res.status(200).json({
      success: true,
      message: 'Your CAP AI account and all associated data have been permanently deleted.'
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Error deleting account.' });
  }
};
