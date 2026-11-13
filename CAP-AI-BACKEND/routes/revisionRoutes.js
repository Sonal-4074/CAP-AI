const express = require('express');
const router = express.Router();
const revisionController = require('../controllers/revisionController');
const { protect } = require('../middleware/authMiddleware');

router.get('/', protect, revisionController.getRevisionList);
router.post('/generate', protect, revisionController.createRevisionSession);
router.post('/:id/complete', protect, revisionController.completeRevisionSession);

module.exports = router;
