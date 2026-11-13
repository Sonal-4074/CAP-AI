const express = require('express');
const router = express.Router();
const quizController = require('../controllers/quizController');
const { protect } = require('../middleware/authMiddleware');

router.post('/submit', protect, quizController.submitQuiz);
router.get('/history', protect, quizController.getQuizHistory);

module.exports = router;
