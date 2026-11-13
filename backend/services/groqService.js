const axios = require('axios');
const db = require('../config/store');

const GROQ_BASE_URL = 'https://api.groq.com/openai/v1';
const DEFAULT_MODEL = process.env.GROQ_MODEL || 'llama-3.3-70b-versatile';
const FALLBACK_MODEL = 'llama-3.1-8b-instant';

class GroqService {
  constructor() {
    this.apiKey = process.env.GROQ_API_KEY || '';
    this.model = DEFAULT_MODEL;
  }

  getApiKey() {
    return process.env.GROQ_API_KEY || this.apiKey;
  }

  getModel() {
    return process.env.GROQ_MODEL || this.model;
  }

  async callGroq(messages, jsonMode = true, retryWithFallback = true) {
    const apiKey = this.getApiKey();
    if (!apiKey) {
      throw new Error('GROQ_API_KEY is not configured in backend environment variables. Please add GROQ_API_KEY to your environment.');
    }

    const payload = {
      model: this.getModel(),
      messages,
      temperature: 0.3,
      max_tokens: 3500
    };

    if (jsonMode) {
      payload.response_format = { type: 'json_object' };
    }

    try {
      const response = await axios.post(`${GROQ_BASE_URL}/chat/completions`, payload, {
        headers: {
          'Authorization': `Bearer ${apiKey}`,
          'Content-Type': 'application/json'
        },
        timeout: 45000
      });

      const content = response.data?.choices?.[0]?.message?.content;
      if (!content) {
        throw new Error('Groq returned an empty response.');
      }

      if (jsonMode) {
        return this.parseAndCleanJson(content);
      }
      return content;
    } catch (err) {
      console.error('Groq API Error:', err.response?.data || err.message);

      // Handle fallback model on rate limits or model-not-found
      const status = err.response?.status;
      if (retryWithFallback && (status === 429 || status === 404) && payload.model !== FALLBACK_MODEL) {
        console.warn(`Retrying Groq request with fallback model: ${FALLBACK_MODEL}`);
        payload.model = FALLBACK_MODEL;
        try {
          const fbResponse = await axios.post(`${GROQ_BASE_URL}/chat/completions`, payload, {
            headers: {
              'Authorization': `Bearer ${apiKey}`,
              'Content-Type': 'application/json'
            },
            timeout: 45000
          });
          const content = fbResponse.data?.choices?.[0]?.message?.content;
          return jsonMode ? this.parseAndCleanJson(content) : content;
        } catch (fbErr) {
          throw this.formatError(fbErr);
        }
      }

      throw this.formatError(err);
    }
  }

  formatError(err) {
    if (err.response) {
      const status = err.response.status;
      const errorMsg = err.response.data?.error?.message || err.message;
      if (status === 401) {
        return new Error('Invalid Groq API Key. Please verify your credentials in Groq console.');
      } else if (status === 429) {
        return new Error('Groq API rate limit reached. Please wait a moment before trying again.');
      } else if (status === 503 || status === 500) {
        return new Error('Groq AI service is temporarily experiencing high load. Please retry.');
      }
      return new Error(`Groq API Error (${status}): ${errorMsg}`);
    } else if (err.code === 'ECONNABORTED' || err.message.includes('timeout')) {
      return new Error('Groq AI request timed out. Please check your network connection.');
    }
    return err;
  }

  parseAndCleanJson(raw) {
    if (typeof raw === 'object' && raw !== null) return raw;
    let clean = raw.trim();
    if (clean.startsWith('```json')) {
      clean = clean.replace(/^```json\s*/, '').replace(/\s*```$/, '');
    } else if (clean.startsWith('```')) {
      clean = clean.replace(/^```\s*/, '').replace(/\s*```$/, '');
    }
    return JSON.parse(clean);
  }

  // 1. Current Affairs: News Summarization & Analysis
  async analyzeNews(article) {
    // Check DB cache first
    const cached = await db.analyses.findOne({ articleId: article.id });
    if (cached) {
      return { ...cached, cached: true };
    }

    const messages = [
      {
        role: 'system',
        content: `You are an expert exam educator and current affairs analyst for Indian competitive examinations (UPSC CSE, SSC CGL, Banking IBPS/SBI, Railway, State PSCs).
Analyze news articles with extreme pedagogical rigor and provide output strictly in JSON format.`
      },
      {
        role: 'user',
        content: `Analyze the following current affairs article and provide comprehensive exam preparation insights:
Title: ${article.title}
Category: ${article.category || 'General'}
Content: ${article.content || article.description}

Required JSON format:
{
  "simpleExplanation": "Clear, accessible 2-3 sentence overview without bureaucratic jargon.",
  "keyFacts": ["High-yield fact 1 (dates, statistics, official bodies)", "Fact 2", "Fact 3", "Fact 4"],
  "examRelevance": "Explanation of direct syllabus mapping (UPSC GS-I/II/III, SSC Tier-1 GA, Banking Financial Awareness).",
  "keywords": [
    {"term": "Term 1", "definition": "Concise examination definition."},
    {"term": "Term 2", "definition": "Concise examination definition."}
  ],
  "backgroundInfo": "Historical, legislative, or policy background necessary to understand root causes.",
  "possibleExamQuestions": [
    "Expected Prelims MCQ or Mains analytical question 1",
    "Expected question 2"
  ],
  "quickRevision": "A high-yield 2-sentence summary for rapid last-minute revision."
}`
      }
    ];

    const result = await this.callGroq(messages, true);

    const record = {
      articleId: article.id,
      simpleExplanation: result.simpleExplanation || 'Detailed analysis processed.',
      keyFacts: Array.isArray(result.keyFacts) ? result.keyFacts : [],
      examRelevance: result.examRelevance || 'Relevant across General Studies.',
      keywords: Array.isArray(result.keywords) ? result.keywords : [],
      backgroundInfo: result.backgroundInfo || 'Historical context established.',
      possibleExamQuestions: Array.isArray(result.possibleExamQuestions) ? result.possibleExamQuestions : [],
      quickRevision: result.quickRevision || 'Quick revision snapshot.'
    };

    // Save in DB cache
    await db.analyses.create(record);
    return record;
  }

  // 2. Interactive Knowledge Maps
  async generateKnowledgeMap(topic, contextText = '') {
    // Check DB cache first
    const cached = await db.knowledgeMaps.findOne({ mainTopic: topic });
    if (cached) {
      return { ...cached, cached: true };
    }

    const messages = [
      {
        role: 'system',
        content: `You are an EdTech knowledge graph architect. Generate structured mind maps connecting concepts, constitutional articles, government bodies, policies, and milestone events for Indian competitive exams.`
      },
      {
        role: 'user',
        content: `Create an interactive Knowledge Map for topic: "${topic}"
Context: ${contextText || 'Core Indian governance, economy, international relations, or science domain.'}

Return JSON with exact structure:
{
  "mainTopic": "${topic}",
  "nodes": [
    {"id": "1", "label": "Main Topic / Headline", "type": "concept", "description": "Core subject description", "parentId": null},
    {"id": "2", "label": "Key Entity / Commission", "type": "organization", "description": "Constitutional body or department", "parentId": "1"},
    {"id": "3", "label": "Governing Scheme / Act", "type": "policy", "description": "Applicable legislation or initiative", "parentId": "1"},
    {"id": "4", "label": "Milestone Event / Date", "type": "event", "description": "Key milestone or timeline event", "parentId": "2"},
    {"id": "5", "label": "Constitutional Provision / Static GK", "type": "fact", "description": "Constitutional Article or core metric", "parentId": "3"}
  ],
  "relationships": [
    {"fromId": "1", "toId": "2", "label": "Administered by"},
    {"fromId": "1", "toId": "3", "label": "Governed by"},
    {"fromId": "2", "toId": "4", "label": "Conducted"},
    {"fromId": "3", "toId": "5", "label": "Derived from"}
  ]
}`
      }
    ];

    const result = await this.callGroq(messages, true);
    const record = {
      mainTopic: result.mainTopic || topic,
      nodes: Array.isArray(result.nodes) ? result.nodes : [],
      relationships: Array.isArray(result.relationships) ? result.relationships : []
    };

    await db.knowledgeMaps.create(record);
    return record;
  }

  // 3. AI Question Generation (MCQs)
  async generateQuestions(articleText, targetExam = 'UPSC', count = 5, difficulty = 'Medium') {
    const messages = [
      {
        role: 'system',
        content: `You are an expert exam question setter for Indian competitive exams (${targetExam}).
Create authentic multiple choice questions following exact examination standards:
- UPSC: Multi-statement, conceptual depth, constitutional/policy questions.
- Banking: Monetary policy, RBI directives, financial figures, fintech schemes.
- SSC CGL: Appointments, summits, sports, science facts, static GK linkages.
Always provide detailed explanations explaining why the correct choice is right and why each distractor is incorrect.`
      },
      {
        role: 'user',
        content: `Generate exactly ${count} multiple choice questions (${difficulty} difficulty) based on:
${articleText}

Return strict JSON format:
{
  "questions": [
    {
      "question": "Question text?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Detailed explanation for why correct option is right and distractors are wrong.",
      "difficulty": "${difficulty}",
      "category": "Current Affairs",
      "relatedConcept": "Key Concept Name"
    }
  ]
}`
      }
    ];

    const result = await this.callGroq(messages, true);
    const questions = Array.isArray(result.questions) ? result.questions : [];

    // Cache generated questions
    for (const q of questions) {
      await db.generatedQuestions.create({
        ...q,
        targetExam,
        sourceTextHash: articleText.slice(0, 100)
      });
    }

    return questions;
  }

  // 4. Question Explanations
  async explainQuestion(question, options, selectedOption, correctOption) {
    const messages = [
      {
        role: 'system',
        content: 'You are an educational tutor explaining competitive exam questions. Provide encouraging, crystal-clear conceptual explanations.'
      },
      {
        role: 'user',
        content: `Explain this question in detail:
Question: ${question}
Options: ${options.join(' | ')}
Student Selected: ${selectedOption}
Correct Answer: ${correctOption}

Return JSON:
{
  "isCorrect": ${selectedOption === correctOption},
  "pedagogicalExplanation": "Deep explanation of the core concept.",
  "whyDistractorsAreWrong": ["Point on distractor 1", "Point on distractor 2"],
  "memoryTrick": "Mnemonics or memory rule to never forget this fact."
}`
      }
    ];

    return await this.callGroq(messages, true);
  }

  // 5. Performance Analysis & Weakness Detection
  async analyzePerformance(quizHistory, topicStats = []) {
    const messages = [
      {
        role: 'system',
        content: `You are an AI learning diagnostic engine for competitive exams.
Evaluate the student's accuracy and topic breakdown against Indian exam benchmarks:
- Below 50%: Weak (Urgent revision required)
- 50% - 69%: Needs Improvement
- 70% - 84%: Good
- 85%+: Strong`
      },
      {
        role: 'user',
        content: `Analyze this user preparation data:
Topic Statistics: ${JSON.stringify(topicStats)}
Recent Quiz History: ${JSON.stringify(quizHistory.slice(0, 5))}

Return JSON:
{
  "overallVerdict": "Summary of student readiness.",
  "weakTopics": ["Topic 1", "Topic 2"],
  "strongTopics": ["Topic 1"],
  "repeatedMistakePatterns": ["Pattern 1 (e.g., confusing monetary policy tools)", "Pattern 2"],
  "actionableAdvice": ["Step 1", "Step 2", "Step 3"]
}`
      }
    ];

    return await this.callGroq(messages, true);
  }

  // 6. Personalized Revision Generation
  async generateRevision(topic, weakConcepts = [], previousAccuracy = 45) {
    // Check DB cache first
    const cached = await db.revisions.findOne({ topic });
    if (cached) {
      return { ...cached, cached: true };
    }

    const conceptsStr = weakConcepts.length > 0 ? weakConcepts.join(', ') : 'Fundamental concepts';
    const messages = [
      {
        role: 'system',
        content: `You are a personalized revision coach for Indian competitive exams.
The student scored ${previousAccuracy}% in "${topic}" and struggled with: ${conceptsStr}.
Provide comprehensive conceptual revision notes and exactly 10 targeted re-test questions to master the topic.`
      },
      {
        role: 'user',
        content: `Generate a personalized revision pack for topic: "${topic}".
Return JSON:
{
  "topic": "${topic}",
  "explanation": "Clear, structured 4-paragraph conceptual revision resolving common misconceptions.",
  "keyFacts": [
    "High-yield fact 1",
    "High-yield fact 2 with figures/dates",
    "High-yield fact 3 with constitutional or economic link",
    "High-yield fact 4"
  ],
  "questions": [
    {
      "question": "Targeted re-test question?",
      "options": ["A", "B", "C", "D"],
      "correctIndex": 0,
      "explanation": "Concise explanation."
    }
  ]
}`
      }
    ];

    const result = await this.callGroq(messages, true);
    const record = {
      topic,
      concepts: weakConcepts,
      explanation: result.explanation || 'Revision notes compiled.',
      keyFacts: Array.isArray(result.keyFacts) ? result.keyFacts : [],
      questions: Array.isArray(result.questions) ? result.questions : [],
      previousAccuracy,
      currentAccuracy: 0.0,
      masteryUpdated: false
    };

    await db.revisions.create(record);
    return record;
  }
}

module.exports = new GroqService();
