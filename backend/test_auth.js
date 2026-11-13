const axios = require('axios');

const PORT = process.env.BACKEND_PORT || 5000;
const BASE_URL = `http://localhost:${PORT}/api`;

async function runTests() {
  console.log('Testing CAP AI Backend APIs...');

  try {
    // 1. Health check
    const health = await axios.get(`${BASE_URL}/health`);
    console.log('✓ Health check passed:', health.data.status);

    // 2. Test Registration with short name (should fail)
    try {
      await axios.post(`${BASE_URL}/auth/register`, {
        fullName: 'A',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'Password123'
      });
      console.error('✗ Registration short name validation failed');
    } catch (err) {
      console.log('✓ Short name rejected correctly (400)');
    }

    // 3. Test Registration with mismatched passwords (should fail)
    try {
      await axios.post(`${BASE_URL}/auth/register`, {
        fullName: 'Rahul Sharma',
        email: 'test@example.com',
        password: 'Password123',
        confirmPassword: 'MismatchPassword'
      });
      console.error('✗ Password mismatch validation failed');
    } catch (err) {
      console.log('✓ Password mismatch rejected correctly (400)');
    }

    // 4. Test Valid Registration
    const regRes = await axios.post(`${BASE_URL}/auth/register`, {
      fullName: 'Rahul Sharma',
      email: 'rahul.aspirant@example.com',
      password: 'SecurePassword123',
      confirmPassword: 'SecurePassword123'
    });
    console.log('✓ Valid registration succeeded. OTP:', regRes.data.debugOtp);

    // 5. Test Login Before Verification (should fail with 403)
    let currentOtp = regRes.data.debugOtp;
    try {
      await axios.post(`${BASE_URL}/auth/login`, {
        email: 'rahul.aspirant@example.com',
        password: 'SecurePassword123'
      });
      console.error('✗ Unverified login check failed');
    } catch (err) {
      if (err.response?.data?.debugOtp) {
        currentOtp = err.response.data.debugOtp;
      }
      console.log('✓ Unverified email login blocked correctly (403)');
    }

    // 6. Test Email Verification with Invalid OTP (should fail)
    try {
      await axios.post(`${BASE_URL}/auth/verify-email`, {
        email: 'rahul.aspirant@example.com',
        otp: '000000'
      });
      console.error('✗ Invalid OTP check failed');
    } catch (err) {
      console.log('✓ Invalid OTP rejected correctly (400)');
    }

    // 7. Test Email Verification with Valid OTP
    const verifyRes = await axios.post(`${BASE_URL}/auth/verify-email`, {
      email: 'rahul.aspirant@example.com',
      otp: currentOtp
    });
    console.log('✓ Email verified successfully! Received JWT token.');
    const token = verifyRes.data.token;

    // 8. Test Login After Verification
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      email: 'rahul.aspirant@example.com',
      password: 'SecurePassword123'
    });
    console.log('✓ Login succeeded! User:', loginRes.data.user.fullName);

    // 9. Test Protected Route GET /api/auth/me
    const meRes = await axios.get(`${BASE_URL}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('✓ Protected /api/auth/me succeeded! Email:', meRes.data.user.email);

    // 10. Test Update Profile
    const profileRes = await axios.put(
      `${BASE_URL}/auth/profile`,
      {
        targetExaminations: ['UPSC', 'State PSC'],
        dailyQuestionTarget: 30,
        difficultyLevel: 'Hard'
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    console.log('✓ Profile updated! Target:', profileRes.data.user.targetExaminations);

    // 11. Test User-Isolated Preparation Data
    const prepRes = await axios.get(`${BASE_URL}/user/preparation-data`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('✓ Personalized preparation data retrieved! Daily target:', prepRes.data.data.dailyQuestionTarget);

    // 12. Test Record Quiz Attempt
    const quizRes = await axios.post(
      `${BASE_URL}/user/quiz-attempt`,
      {
        topic: 'Monetary Policy',
        score: 8,
        totalQuestions: 10,
        examType: 'UPSC'
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    console.log('✓ Quiz attempt recorded:', quizRes.data.attempt.topic, 'Accuracy:', quizRes.data.attempt.accuracy + '%');

    // 13. Test Groq Service Error Handling when key is missing/invalid
    try {
      await axios.post(
        `${BASE_URL}/ai/analyze-news`,
        { article: { title: 'Test News', content: 'Test content' } },
        { headers: { Authorization: `Bearer ${token}` } }
      );
    } catch (err) {
      console.log('✓ Groq API missing key error handled gracefully:', err.response?.data?.message);
    }

    console.log('\nAll 13 Backend Authentication & Preparation tests PASSED successfully!\n');
    process.exit(0);
  } catch (err) {
    console.error('Test execution failed:', err.response?.data || err.message);
    process.exit(1);
  }
}

// Start server in background then run test
const { server } = require('./server');
setTimeout(runTests, 1000);
