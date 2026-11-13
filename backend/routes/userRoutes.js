const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { authenticateToken } = require('../middleware/auth');

// Protected User routes
router.get('/preparation-data', authenticateToken, userController.getPreparationData);
router.post('/quiz-attempt', authenticateToken, userController.recordQuizAttempt);
router.post('/bookmarks', authenticateToken, userController.toggleBookmark);
router.get('/bookmarks', authenticateToken, userController.getBookmarks);

module.exports = router;
