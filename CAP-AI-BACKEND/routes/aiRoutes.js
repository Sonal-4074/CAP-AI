const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');
const { optionalAuth } = require('../middleware/authMiddleware');

router.post('/analyze-news', optionalAuth, aiController.analyzeNews);
router.post('/knowledge-map', optionalAuth, aiController.generateKnowledgeMap);
router.post('/generate-questions', optionalAuth, aiController.generateQuestions);
router.post('/explain-question', optionalAuth, aiController.explainQuestion);
router.post('/revision', optionalAuth, aiController.generateRevision);

module.exports = router;
