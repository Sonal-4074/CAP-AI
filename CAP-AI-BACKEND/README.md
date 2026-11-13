# CAP AI – Production Backend Architecture
**Current Affairs Preparation with AI for Competitive Examinations (UPSC, State PSC, SSC, Banking)**

The official REST API backend for the CAP AI Android mobile application, engineered with Node.js, Express.js, MongoDB Atlas (Mongoose), JWT authentication, and Groq AI inference.

---

## 📁 Project Directory Structure

```text
CAP-AI-BACKEND/
├── config/
│   ├── database.js          # MongoDB Atlas connection with Mongoose
│   └── store.js             # High-reliability dual persistence layer (Atlas / persistent fallback)
├── controllers/
│   ├── authController.js    # Registration, OTP verification, login, profile, password reset
│   ├── newsController.js    # Current affairs feed, filtering by category, search
│   ├── aiController.js      # Groq AI news analysis, Knowledge Maps, MCQs, explanations
│   ├── quizController.js    # Quiz submission, scoring, and performance tracking
│   ├── performanceController.js # Weakness analysis, preparation stats, bookmarks
│   └── revisionController.js # AI-generated revision sessions and mastery updates
├── middleware/
│   └── authMiddleware.js    # JWT Bearer token authentication & route protection
├── models/
│   ├── User.js              # User schema with bcrypt hashing & OTPs
│   ├── CurrentAffairs.js    # Exam-oriented news articles schema
│   ├── KnowledgeMap.js      # Concept nodes & relationships schema
│   ├── Question.js          # Exam questions & options schema
│   ├── QuizAttempt.js       # Quiz submissions & scores schema
│   ├── UserPerformance.js   # Accuracy, weak topics & stats schema
│   └── RevisionHistory.js   # Revision sessions & mastery schema
├── routes/
│   ├── authRoutes.js        # /api/auth routes
│   ├── newsRoutes.js        # /api/news routes
│   ├── aiRoutes.js          # /api/ai routes
│   ├── quizRoutes.js        # /api/quiz routes
│   ├── performanceRoutes.js # /api/performance routes
│   └── revisionRoutes.js    # /api/revision routes
├── services/
│   ├── groqService.js       # Groq OpenAI-compatible AI service (llama-3.3-70b-versatile)
│   ├── newsService.js       # Curated current affairs data & retrieval
│   └── emailService.js      # Nodemailer OTP email delivery service
├── .env.example             # Environment configuration template
├── package.json             # Dependencies and npm scripts
├── server.js                # Express app entrypoint listening on 0.0.0.0:5000
└── README.md                # Documentation & local setup instructions
```

---

## ⚡ Quick Start: Running Locally

### 1. Prerequisites
- **Node.js** (v18.x, v20.x, or v22.x)
- **npm** (v9+) or **yarn**

### 2. Installation
Navigate into the `CAP-AI-BACKEND` directory and install dependencies:

```bash
cd CAP-AI-BACKEND
npm install
```

### 3. Environment Configuration
Create your `.env` file from `.env.example`:

```bash
cp .env.example .env
```

Open `.env` and configure your credentials:

```ini
PORT=5000
HOST=0.0.0.0
NODE_ENV=development

# Groq API Configuration
GROQ_API_KEY=gsk_your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile

# MongoDB Atlas Database URI
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.abcde.mongodb.net/cap_ai?retryWrites=true&w=majority

# JWT Authentication Secret
JWT_SECRET=your_super_secret_jwt_key_2026

# Email Delivery (Optional for sending real OTP emails via Gmail)
EMAIL_USER=your_email@gmail.com
EMAIL_PASS=your_gmail_app_password
EMAIL_FROM="CAP AI Support <noreply@capai.edu>"
```

### 4. Start the Server
To start in development mode with live reload:
```bash
npm run dev
```

Or start standard production node process:
```bash
npm start
```

The server will initialize on:
- Host: `http://0.0.0.0:5000`
- Android Emulator URL: `http://10.0.2.2:5000`
- Health Check: `http://localhost:5000/api/health`

---

## 🍃 MongoDB Atlas Setup Guide

To connect the backend to your free MongoDB Atlas cloud database:

1. **Create an Account**: Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register) and sign up.
2. **Deploy a Free Cluster**:
   - Choose the **M0 Free Tier**.
   - Select your preferred cloud provider and closest region (e.g., Mumbai, AWS).
3. **Create Database User**:
   - Navigate to **Security** -> **Database Access**.
   - Click **Add New Database User**.
   - Choose **Password** authentication.
   - Enter a username and secure password (e.g. `cap_admin` / `YourSecretPassword123`).
   - Grant `Read and write to any database` privileges.
4. **Configure IP Access**:
   - Navigate to **Security** -> **Network Access**.
   - Click **Add IP Address**.
   - Click **Allow Access from Anywhere** (`0.0.0.0/0`) or enter your current IP.
5. **Get Connection String**:
   - Go to **Databases** -> Click **Connect** on your cluster.
   - Choose **Drivers** (Node.js).
   - Copy the connection string format:
     ```text
     mongodb+srv://cap_admin:<password>@cluster0.abcde.mongodb.net/cap_ai?retryWrites=true&w=majority
     ```
   - Replace `<password>` with your database user password and paste into `.env` under `MONGODB_URI`.

*Note*: If `MONGODB_URI` is not set or network is unreachable, CAP AI automatically activates its local persistent document store so the Android app and tests run without any interruption!

---

## 🤖 Groq AI Integration Guide

1. Sign up at [Groq Console](https://console.groq.com).
2. Generate an API Key under **API Keys**.
3. Set `GROQ_API_KEY=gsk_...` in your `.env` file.
4. Supported Models:
   - `llama-3.3-70b-versatile` (Default recommended: high reasoning, large context)
   - `llama-3.1-8b-instant` (Ultra fast responses)
   - `mixtral-8x7b-32768`

---

## 📱 Android Connection Setup

Android emulators communicate with the host development machine via the special loopback IP:
```text
http://10.0.2.2:5000/
```

This is configured in the Android frontend in `ApiClient.kt`:
```kotlin
private const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"
```

Network traffic permissions in `app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<application
    android:usesCleartextTraffic="true"
    ...>
```

---

## 📋 API Specification Summary

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Create account & send 6-digit email OTP.
- `POST /api/auth/verify-email` - Verify email OTP & receive JWT token.
- `POST /api/auth/resend-otp` - Resend fresh email OTP.
- `POST /api/auth/login` - Authenticate with email/password.
- `POST /api/auth/forgot-password` - Request password reset code.
- `POST /api/auth/reset-password` - Reset password with OTP.
- `GET /api/auth/me` - Get current authenticated user profile *(Bearer token)*.
- `PUT /api/auth/profile` - Update target exams, difficulty & preferences *(Bearer token)*.
- `POST /api/auth/logout` - Invalidate session *(Bearer token)*.
- `DELETE /api/auth/account` - Permanently delete account and data *(Bearer token)*.

### Groq AI Processing (`/api/ai`)
- `POST /api/ai/analyze-news` - Generate exam-oriented analysis, key facts & GS paper relevance.
- `POST /api/ai/knowledge-map` - Generate interactive node/edge concept map.
- `POST /api/ai/generate-questions` - Generate tailored exam MCQs with explanations.
- `POST /api/ai/explain-question` - Generate pedagogical explanation of question choices.
- `POST /api/ai/revision` - Generate adaptive revision session for weak topics.

### Current Affairs News (`/api/news`)
- `GET /api/news` - Retrieve paginated articles (filtered by `category`, `search`).
- `GET /api/news/:id` - Retrieve full article details.
- `POST /api/news` - Publish new article *(Bearer token)*.

### Quizzes & Performance (`/api/quiz`, `/api/performance`, `/api/user`)
- `POST /api/quiz/submit` - Record quiz attempt & update topic accuracies.
- `GET /api/quiz/history` - User's past quiz history.
- `GET /api/performance` - Detailed performance metrics & topic breakdown.
- `GET /api/performance/weaknesses` - Weakness detection & recommendations.
- `GET /api/user/preparation-data` - Android dashboard aggregate preparation stats.
- `POST /api/user/quiz-attempt` - Android compatibility quiz submission.
- `POST /api/user/bookmarks` - Toggle bookmarked articles.

### Revision (`/api/revision`)
- `GET /api/revision` - User revision history.
- `POST /api/revision/generate` - Generate adaptive revision session.
- `POST /api/revision/:id/complete` - Mark revision session completed.
