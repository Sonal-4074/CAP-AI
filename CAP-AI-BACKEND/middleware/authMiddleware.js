const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'cap_ai_super_secret_jwt_key_competitive_exams_2026_xyz987';

/**
 * Middleware to authenticate requests using JWT Bearer token.
 * Populates req.user with { userId, email }.
 */
const protect = async (req, res, next) => {
  let token = null;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer ')
  ) {
    token = req.headers.authorization.split(' ')[1];
  }

  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'Access denied. No authentication token provided.'
    });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = {
      userId: decoded.id || decoded.userId,
      email: decoded.email
    };
    next();
  } catch (error) {
    return res.status(401).json({
      success: false,
      message: 'Authentication token is invalid or has expired. Please log in again.'
    });
  }
};

/**
 * Optional authentication: populates req.user if token is present, continues otherwise.
 */
const optionalAuth = (req, res, next) => {
  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer ')
  ) {
    const token = req.headers.authorization.split(' ')[1];
    try {
      const decoded = jwt.verify(token, JWT_SECRET);
      req.user = {
        userId: decoded.id || decoded.userId,
        email: decoded.email
      };
    } catch {
      // Ignored for optional auth
    }
  }
  next();
};

module.exports = { protect, optionalAuth, JWT_SECRET };
