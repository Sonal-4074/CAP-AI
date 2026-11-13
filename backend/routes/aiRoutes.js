const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');
const { authenticateToken } = require('../middleware/auth');

// Protected AI routes
router.post('/analyze-news', authenticateToken, aiController.analyzeNews);
router.post('/knowledge-map', authenticateToken, aiController.generateKnowledgeMap);
router.post('/generate-questions', authenticateToken, aiController.generateQuestions);
router.post('/explain-question', authenticateToken, aiController.explainQuestion);
router.post('/performance-analysis', authenticateToken, aiController.analyzePerformance);
router.post('/revision', authenticateToken, aiController.generateRevision);

module.exports = router;
