const axios = require('axios');

const BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:5000';
let authToken = '';
let testEmail = `aspirant_${Date.now()}@capai.edu`;
let testOtp = '';

async function runTestSuite() {
  console.log(`\n======================================================`);
  console.log(`🧪 Running Comprehensive CAP AI Backend API Tests`);
  console.log(`Target: ${BASE_URL}`);
  console.log(`======================================================\n`);

  let passed = 0;
  let failed = 0;

  async function test(name, fn) {
    try {
      await fn();
      console.log(`✅ [PASS] ${name}`);
      passed++;
    } catch (err) {
      console.error(`❌ [FAIL] ${name}:`, err.response?.data?.message || err.message);
      failed++;
    }
  }

  // 1. Health check
  await test('GET /api/health', async () => {
    const res = await axios.get(`${BASE_URL}/api/health`);
    if (res.data.status !== 'online') throw new Error('Status not online');
  });

  // 2. User registration
  await test('POST /api/auth/register', async () => {
    const res = await axios.post(`${BASE_URL}/api/auth/register`, {
      fullName: 'Aarav Sharma',
      email: testEmail,
      password: 'Password@123',
      confirmPassword: 'Password@123'
    });
    if (!res.data.success) throw new Error('Registration failed');
    testOtp = res.data.debugOtp || '123456';
  });

  // 3. Email OTP verification
  await test('POST /api/auth/verify-email', async () => {
    const res = await axios.post(`${BASE_URL}/api/auth/verify-email`, {
      email: testEmail,
      otp: testOtp
    });
    if (!res.data.success || !res.data.token) throw new Error('Verification failed');
    authToken = res.data.token;
  });

  // 4. User login
  await test('POST /api/auth/login', async () => {
    const res = await axios.post(`${BASE_URL}/api/auth/login`, {
      email: testEmail,
      password: 'Password@123'
    });
    if (!res.data.success || !res.data.token) throw new Error('Login failed');
  });

  // 5. Get current profile (GET /api/auth/me)
  await test('GET /api/auth/me', async () => {
    const res = await axios.get(`${BASE_URL}/api/auth/me`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    if (!res.data.success || res.data.user.email !== testEmail) throw new Error('Profile mismatch');
  });

  // 6. Update profile (PUT /api/auth/profile)
  await test('PUT /api/auth/profile', async () => {
    const res = await axios.put(
      `${BASE_URL}/api/auth/profile`,
      {
        targetExaminations: ['UPSC', 'State PSC'],
        difficultyLevel: 'Hard',
        dailyQuestionTarget: 30
      },
      { headers: { Authorization: `Bearer ${authToken}` } }
    );
    if (!res.data.success || res.data.user.difficultyLevel !== 'Hard') throw new Error('Update failed');
  });

  // 7. News retrieval
  await test('GET /api/news', async () => {
    const res = await axios.get(`${BASE_URL}/api/news`);
    if (!res.data.success || !Array.isArray(res.data.data)) throw new Error('News list failed');
  });

  // 8. AI News analysis
  await test('POST /api/ai/analyze-news', async () => {
    const res = await axios.post(`${BASE_URL}/api/ai/analyze-news`, {
      article: {
        id: 'test_art_1',
        title: 'Deep Ocean Mission and Exploration in India',
        summary: 'India is advancing polymetallic nodule exploration under ISA regulations.',
        category: 'Science & Tech'
      }
    });
    if (!res.data.success || !res.data.data.simpleExplanation) throw new Error('AI analysis failed');
  });

  // 9. AI Knowledge Map
  await test('POST /api/ai/knowledge-map', async () => {
    const res = await axios.post(`${BASE_URL}/api/ai/knowledge-map`, {
      topic: 'Monetary Policy Committee',
      contextText: 'Formed under Section 45ZB of the amended RBI Act 1934 to target CPI inflation.'
    });
    if (!res.data.success || !res.data.data.nodes) throw new Error('Knowledge map failed');
  });

  // 10. AI MCQ Generation
  await test('POST /api/ai/generate-questions', async () => {
    const res = await axios.post(`${BASE_URL}/api/ai/generate-questions`, {
      articleText: 'The Supreme Court ruled that sub-classification within reserved categories is constitutionally permissible.',
      targetExam: 'UPSC',
      count: 2,
      difficulty: 'Medium'
    });
    if (!res.data.success || !Array.isArray(res.data.data)) throw new Error('MCQ generation failed');
  });

  // 11. Quiz submission
  await test('POST /api/quiz/submit', async () => {
    const res = await axios.post(
      `${BASE_URL}/api/quiz/submit`,
      {
        topic: 'Indian Polity - Supreme Court Bench',
        score: 4,
        totalQuestions: 5,
        examType: 'UPSC'
      },
      { headers: { Authorization: `Bearer ${authToken}` } }
    );
    if (!res.data.success) throw new Error('Quiz submit failed');
  });

  // 12. Android User Preparation Data
  await test('GET /api/user/preparation-data', async () => {
    const res = await axios.get(`${BASE_URL}/api/user/preparation-data`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    if (!res.data.success || res.data.data.totalAttempted !== 5) throw new Error('Preparation data failed');
  });

  // 13. Bookmark toggle
  await test('POST /api/user/bookmarks', async () => {
    const res = await axios.post(
      `${BASE_URL}/api/user/bookmarks`,
      {
        articleId: 'test_art_1',
        title: 'Deep Ocean Mission',
        category: 'Science & Tech'
      },
      { headers: { Authorization: `Bearer ${authToken}` } }
    );
    if (!res.data.success) throw new Error('Bookmark toggle failed');
  });

  console.log(`\n======================================================`);
  console.log(`Test Results: ${passed} Passed, ${failed} Failed`);
  console.log(`======================================================\n`);

  if (failed > 0) process.exit(1);
}

runTestSuite().catch(err => {
  console.error('Test run failed:', err);
  process.exit(1);
});
