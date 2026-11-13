const express = require('express');
const cors = require('cors');
const path = require('path');
const dotenv = require('dotenv');

// Load environment variables from local .env or parent .env
dotenv.config({ path: path.join(__dirname, '.env') });
dotenv.config({ path: path.join(__dirname, '..', '.env') });

const { connectDB, getIsConnected } = require('./config/database');

const app = express();
const PORT = process.env.CAP_PORT || 5000;
const HOST = process.env.HOST || '0.0.0.0';

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Request logger for debugging Android emulator traffic
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl} - ${res.statusCode} (${duration}ms)`);
  });
  next();
});

// Import Routes
const authRoutes = require('./routes/authRoutes');
const newsRoutes = require('./routes/newsRoutes');
const aiRoutes = require('./routes/aiRoutes');
const quizRoutes = require('./routes/quizRoutes');
const performanceRoutes = require('./routes/performanceRoutes');
const revisionRoutes = require('./routes/revisionRoutes');

// Import Controllers for Android User API compatibility
const performanceController = require('./controllers/performanceController');
const quizController = require('./controllers/quizController');
const { protect } = require('./middleware/authMiddleware');

// Mount Routes
app.use('/api/auth', authRoutes);
app.use('/api/news', newsRoutes);
app.use('/api/ai', aiRoutes);
app.use('/api/quiz', quizRoutes);
app.use('/api/performance', performanceRoutes);
app.use('/api/revision', revisionRoutes);

// Dedicated Android user routes (/api/user/*) for 100% frontend compatibility
app.get('/api/user/preparation-data', protect, performanceController.getPreparationData);
app.post('/api/user/quiz-attempt', protect, quizController.submitQuiz);
app.post('/api/user/bookmarks', protect, performanceController.toggleBookmark);

// Health check endpoint (Requirement 5)
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'online',
    app: 'CAP AI Backend',
    version: '1.0.0',
    port: PORT,
    host: HOST,
    mongoConnected: getIsConnected(),
    groqConfigured: !!(process.env.GROQ_API_KEY && process.env.GROQ_API_KEY !== 'your_groq_api_key_here'),
    groqModel: process.env.GROQ_MODEL || 'llama-3.3-70b-versatile',
    timestamp: new Date().toISOString()
  });
});

// Root ping
app.get('/', (req, res) => {
  res.status(200).json({
    message: 'CAP AI REST API Server is operational',
    docs: 'Refer to README.md for endpoint specifications'
  });
});

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `API endpoint '${req.originalUrl}' not found.`
  });
});

// Centralized error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled server error:', err);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal server error occurred.'
  });
});

// Start Server and initialize MongoDB connection
let server;

async function startServer() {
  await connectDB();

  server = app.listen(PORT, HOST, () => {
    console.log(`\n======================================================`);
    console.log(`🚀 CAP AI Node.js Backend is RUNNING!`);
    console.log(`📡 Listening on: http://${HOST}:${PORT}`);
    console.log(`📱 Android Emulator URL: http://10.0.2.2:${PORT}`);
    console.log(`🩺 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`🤖 Groq Model: ${process.env.GROQ_MODEL || 'llama-3.3-70b-versatile'}`);
    console.log(`🔑 Groq API Key Configured: ${!!(process.env.GROQ_API_KEY && process.env.GROQ_API_KEY !== 'your_groq_api_key_here')}`);
    console.log(`🍃 MongoDB Atlas Status: ${getIsConnected() ? 'CONNECTED' : 'LOCAL FALLBACK ACTIVE'}`);
    console.log(`======================================================\n`);
  });
}

startServer();

process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection at:', promise, 'reason:', reason);
});

// Keep process active in daemon containers
setInterval(() => {}, 1000 * 60 * 60);

module.exports = { app, startServer };
