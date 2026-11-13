const express = require('express');
const router = express.Router();
const newsController = require('../controllers/newsController');
const { protect } = require('../middleware/authMiddleware');

router.get('/', newsController.getArticles);
router.get('/:id', newsController.getArticleById);
router.post('/', protect, newsController.createArticle);

module.exports = router;
