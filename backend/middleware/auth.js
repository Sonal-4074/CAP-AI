const jwt = require('jsonwebtoken');
const db = require('../config/store');

const JWT_SECRET = process.env.JWT_SECRET || 'cap_ai_jwt_super_secret_key_2026_exam_prep';

async function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.startsWith('Bearer ') ? authHeader.split(' ')[1] : null;

  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'Access denied. No authentication token provided.'
    });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await db.users.findById(decoded.userId);

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid session. User account does not exist.'
      });
    }

    if (!user.isEmailVerified) {
      return res.status(403).json({
        success: false,
        isEmailVerified: false,
        message: 'Email not verified. Please verify your email with the OTP sent.'
      });
    }

    req.user = user;
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        expired: true,
        message: 'Your session has expired. Please log in again.'
      });
    }
    return res.status(401).json({
      success: false,
      message: 'Invalid authentication token.'
    });
  }
}

module.exports = {
  authenticateToken,
  JWT_SECRET
};
