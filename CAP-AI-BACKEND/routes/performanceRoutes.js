const express = require('express');
const router = express.Router();
const performanceController = require('../controllers/performanceController');
const { protect } = require('../middleware/authMiddleware');

router.get('/', protect, performanceController.getPerformance);
router.get('/weaknesses', protect, performanceController.getWeaknesses);

module.exports = router;
