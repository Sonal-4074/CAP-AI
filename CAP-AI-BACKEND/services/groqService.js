const axios = require('axios');

const GROQ_BASE_URL = 'https://api.groq.com/openai/v1/chat/completions';
const DEFAULT_MODEL = 'llama-3.3-70b-versatile';
const FALLBACK_MODEL = 'llama-3.1-8b-instant';

/**
 * Core reusable function to call the Groq API (OpenAI-compatible chat completion).
 * Automatically handles retry with fallback model if the primary model hits rate limits or is busy.
 */
async function callGroqChat(messages, options = {}) {
  const apiKey = process.env.GROQ_API_KEY;

  if (!apiKey || apiKey === 'your_groq_api_key_here') {
    throw new Error('GROQ_API_KEY_NOT_CONFIGURED');
  }

  const primaryModel = process.env.GROQ_MODEL || DEFAULT_MODEL;
  const temperature = options.temperature ?? 0.3;
  const maxTokens = options.maxTokens ?? 2048;
  const jsonMode = options.jsonMode ?? false;

  const requestBody = {
    model: primaryModel,
    messages,
    temperature,
    max_tokens: maxTokens,
  };

  if (jsonMode) {
    requestBody.response_format = { type: 'json_object' };
  }

  try {
    const response = await axios.post(GROQ_BASE_URL, requestBody, {
      headers: {
        Authorization: `Bearer ${apiKey}`,
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    });

    const content = response.data?.choices?.[0]?.message?.content;
    if (!content) {
      throw new Error('Empty response from Groq API');
    }
    return content;
  } catch (error) {
    // If rate-limited or error with primary model, attempt fallback model
    if (primaryModel !== FALLBACK_MODEL && error.response?.status !== 401) {
      console.warn(`Groq primary model failed (${error.message}). Retrying with fallback model ${FALLBACK_MODEL}...`);
      try {
        requestBody.model = FALLBACK_MODEL;
        const response = await axios.post(GROQ_BASE_URL, requestBody, {
          headers: {
            Authorization: `Bearer ${apiKey}`,
            'Content-Type': 'application/json',
          },
          timeout: 25000,
        });
        const content = response.data?.choices?.[0]?.message?.content;
        if (content) return content;
      } catch (fallbackErr) {
        console.error('Groq fallback attempt also failed:', fallbackErr.message);
      }
    }
    throw error;
  }
}

/**
 * 1. Analyze News Article for Competitive Exam relevance (GS Papers, Key Facts, Keywords).
 */
async function analyzeNews(article) {
  const prompt = `You are an expert exam mentor for Indian Civil Services (UPSC, State PSC, SSC).
Analyze the following news article and return a strictly valid JSON response adhering exactly to this schema:
{
  "articleId": "${article.id || ''}",
  "simpleExplanation": "Clear, concise 2-3 sentence overview suitable for exam aspirants",
  "keyFacts": ["Fact 1 with dates, figures, or constitutional articles", "Fact 2", "Fact 3", "Fact 4"],
  "examRelevance": "Which paper/syllabus section this maps to (e.g. GS Paper II - Governance, Constitution)",
  "keywords": [
    {"term": "Term 1", "definition": "Direct exam definition"},
    {"term": "Term 2", "definition": "Direct exam definition"}
  ],
  "backgroundInfo": "Historical context, statutory background, or constitutional provisions related to this topic",
  "possibleExamQuestions": [
    "Sample Prelims or Mains question 1",
    "Sample Prelims or Mains question 2"
  ],
  "quickRevision": "High-yield one-liner summary for last-minute revision"
}

Article Title: ${article.title}
Category: ${article.category || 'General'}
Content: ${article.summary || article.content || ''}`;

  try {
    const raw = await callGroqChat(
      [
        { role: 'system', content: 'You are an educational AI assistant that outputs strictly valid JSON.' },
        { role: 'user', content: prompt },
      ],
      { jsonMode: true }
    );
    return JSON.parse(raw);
  } catch (err) {
    console.warn(`Groq analyzeNews fallback: ${err.message}`);
    return generateFallbackAnalysis(article);
  }
}

/**
 * 2. Generate Interactive Knowledge Map (Nodes, Edges, Concepts, Relationships).
 */
async function generateKnowledgeMap(topic, contextText = '') {
  const prompt = `You are a curriculum architect for competitive examinations.
Create a structured Knowledge Map for the topic "${topic}".
Context: ${contextText || topic}

Return a strictly valid JSON response adhering exactly to this schema:
{
  "mainTopic": "${topic}",
  "nodes": [
    {
      "id": "1",
      "label": "Short Title",
      "type": "concept", // must be one of: "concept", "organization", "policy", "event", "fact"
      "description": "Crisp 1-sentence explanation of this node",
      "parentId": null // or id of parent node
    }
  ],
  "relationships": [
    {
      "fromId": "1",
      "toId": "2",
      "label": "influences / implements / governs / regulates"
    }
  ]
}

Provide 6-10 interconnected nodes and meaningful relationship labels.`;

  try {
    const raw = await callGroqChat(
      [
        { role: 'system', content: 'You are an educational AI assistant that outputs strictly valid JSON.' },
        { role: 'user', content: prompt },
      ],
      { jsonMode: true }
    );
    return JSON.parse(raw);
  } catch (err) {
    console.warn(`Groq generateKnowledgeMap fallback: ${err.message}`);
    return generateFallbackKnowledgeMap(topic);
  }
}

/**
 * 3. Generate Exam-Oriented Multiple Choice Questions (MCQs).
 */
async function generateQuestions(articleText, targetExam = 'UPSC', count = 5, difficulty = 'Medium') {
  const prompt = `You are a senior exam paper setter for ${targetExam}.
Generate ${count} high-standard multiple choice questions based on the following material.
Difficulty Level: ${difficulty}

Content:
${articleText}

Return a strictly valid JSON object with a "questions" array adhering to this schema:
{
  "questions": [
    {
      "id": "q1",
      "question": "Clear, rigorous question stem",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0, // Integer 0, 1, 2, or 3
      "explanation": "Detailed explanation clarifying why the correct option is right and others are incorrect.",
      "difficulty": "${difficulty}",
      "category": "Current Affairs",
      "relatedConcept": "Key syllabus topic",
      "examType": "${targetExam}"
    }
  ]
}`;

  try {
    const raw = await callGroqChat(
      [
        { role: 'system', content: 'You are an exam paper setter who outputs strictly valid JSON.' },
        { role: 'user', content: prompt },
      ],
      { jsonMode: true }
    );
    const parsed = JSON.parse(raw);
    return parsed.questions || parsed.data || [];
  } catch (err) {
    console.warn(`Groq generateQuestions fallback: ${err.message}`);
    return generateFallbackQuestions(targetExam, count, difficulty);
  }
}

/**
 * 4. Generate Question Conceptual Explanation.
 */
async function explainQuestion(question, options, selectedOption, correctOption) {
  const prompt = `Explain why for the question:
"${question}"

Options: ${JSON.stringify(options)}
The user selected: "${selectedOption}"
The correct answer is: "${correctOption}"

Provide an educational explanation:
1. Why the correct option is right.
2. Why the selected option is a common distractor or incorrect.
3. Essential exam takeaway to remember.`;

  try {
    return await callGroqChat([
      { role: 'system', content: 'You are an encouraging and precise exam educator.' },
      { role: 'user', content: prompt },
    ]);
  } catch (err) {
    return `Correct Answer: ${correctOption}. Detailed explanation: The question tests core conceptual understanding. Remember to verify the statutory mandate and jurisdictional bounds under standard exam criteria.`;
  }
}

/**
 * 5. Generate Personalized Revision Plan & Questions.
 */
async function generateRevision(topic, weakConcepts = [], previousAccuracy = 45.0) {
  const prompt = `The student has an accuracy of ${previousAccuracy}% in "${topic}".
Weak areas identified: ${weakConcepts.join(', ') || 'Core concepts and factual timelines'}.

Create a targeted micro-revision session. Return strictly valid JSON:
{
  "topic": "${topic}",
  "concepts": ["Concept 1", "Concept 2"],
  "explanation": "Structured revision summary highlighting common pitfalls and conceptual clarity",
  "keyFacts": ["Key Fact 1", "Key Fact 2", "Key Fact 3"],
  "questions": [
    {
      "id": "rev1",
      "question": "Practice question targeting the weakness",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 1,
      "explanation": "Why B is correct",
      "difficulty": "Medium",
      "category": "${topic}",
      "relatedConcept": "Weak concept",
      "examType": "UPSC"
    }
  ],
  "previousAccuracy": ${previousAccuracy},
  "currentAccuracy": 0.0,
  "masteryUpdated": false
}`;

  try {
    const raw = await callGroqChat(
      [
        { role: 'system', content: 'You are an adaptive educational mentor outputting strictly valid JSON.' },
        { role: 'user', content: prompt },
      ],
      { jsonMode: true }
    );
    return JSON.parse(raw);
  } catch (err) {
    console.warn(`Groq generateRevision fallback: ${err.message}`);
    return generateFallbackRevision(topic, weakConcepts, previousAccuracy);
  }
}

// ------------------------------------------------------------------------------
// Fallback Generators (Ensures 100% Reliability for Local & Offline Demos)
// ------------------------------------------------------------------------------

function generateFallbackAnalysis(article) {
  return {
    articleId: article.id || 'article_default',
    simpleExplanation: `${article.title || 'Current Affairs'} is a high-priority development in Indian governance and policy with direct implications for competitive examinations.`,
    keyFacts: [
      'Constitutional benchmark: Relevant under Articles 14, 21, and Directive Principles.',
      'Implementation authority: NITI Aayog and Central Ministries.',
      'Socio-economic impact: Directly affects inclusive growth and infrastructure milestones.',
      'Exam relevance: High probability in Prelims GS-I and Mains GS-II/III.',
    ],
    examRelevance: 'GS Paper II & III: Governance, Government Policies, and Economic Growth.',
    keywords: [
      { term: 'Statutory Body', definition: 'A non-constitutional body set up by an Act of Parliament.' },
      { term: 'Fiscal Deficit', definition: 'The difference between the total income of the government and its total expenditure.' },
    ],
    backgroundInfo: 'First initiated under national reform schemes; periodic reviews are published by parliamentary standing committees.',
    possibleExamQuestions: [
      'Consider the following statements regarding statutory regulatory authorities in India...',
      'Critically evaluate the institutional framework governing this sector with reference to recent legislative changes.',
    ],
    quickRevision: 'Key focus: Verify enforcing ministry, constitutional roots, and primary targets for upcoming prelims.',
  };
}

function generateFallbackKnowledgeMap(topic) {
  return {
    mainTopic: topic,
    nodes: [
      { id: '1', label: topic, type: 'concept', description: 'Central theme of examination syllabus', parentId: null },
      { id: '2', label: 'Constitutional Basis', type: 'concept', description: 'Underlying Articles and Fundamental Framework', parentId: '1' },
      { id: '3', label: 'Key Legislation', type: 'policy', description: 'Enacted parliamentary acts and statutory rules', parentId: '1' },
      { id: '4', label: 'Implementing Agency', type: 'organization', description: 'Designated ministries and regulatory commissions', parentId: '1' },
      { id: '5', label: 'Recent Policy Amendments', type: 'event', description: 'Latest executive notifications and budget allocations', parentId: '3' },
      { id: '6', label: 'High-Yield Facts', type: 'fact', description: 'Direct numbers, committees, and landmark apex court rulings', parentId: '2' },
    ],
    relationships: [
      { fromId: '1', toId: '2', label: 'derives authority from' },
      { fromId: '1', toId: '3', label: 'governed by' },
      { fromId: '3', toId: '4', label: 'administered through' },
      { fromId: '4', toId: '5', label: 'executes' },
      { fromId: '2', toId: '6', label: 'substantiated by' },
    ],
  };
}

function generateFallbackQuestions(targetExam, count, difficulty) {
  const sample = [
    {
      id: 'q_' + Math.random().toString(36).substring(7),
      question: 'With reference to the National Green Hydrogen Mission, consider the following statements:\n1. It aims to make India a global hub for the production and export of Green Hydrogen.\n2. The Ministry of Power is the sole nodal ministry for its implementation.\nWhich of the statements given above is/are correct?',
      options: ['1 only', '2 only', 'Both 1 and 2', 'Neither 1 nor 2'],
      correctIndex: 0,
      explanation: 'Statement 1 is correct. Statement 2 is incorrect because the Ministry of New and Renewable Energy (MNRE) is the primary nodal ministry.',
      difficulty,
      category: 'Environment & Energy',
      relatedConcept: 'Green Hydrogen Mission',
      examType: targetExam,
    },
    {
      id: 'q_' + Math.random().toString(36).substring(7),
      question: 'The term "Base Effect", frequently seen in economic news, most directly relates to which of the following?',
      options: [
        'The impact of the price levels of the previous year on calculation of current inflation',
        'The minimum capital requirement mandated by Basel III norms for scheduled banks',
        'The baseline emissions year established under the Paris Climate Agreement',
        'The foundation tax slab for Corporate Social Responsibility obligations',
      ],
      correctIndex: 0,
      explanation: 'Base effect refers to the distortion in a monthly or annual inflation figure caused by unusually high or low price levels in the corresponding previous period.',
      difficulty,
      category: 'Economy',
      relatedConcept: 'Inflation & GDP Calculation',
      examType: targetExam,
    },
  ];
  return sample.slice(0, count);
}

function generateFallbackRevision(topic, weakConcepts, previousAccuracy) {
  return {
    topic,
    concepts: weakConcepts.length > 0 ? weakConcepts : ['Statutory Provisions', 'Chronology of Reforms', 'Constitutional Mandate'],
    explanation: `This revision session addresses identified learning gaps in ${topic}. Focus on isolating regulatory mandates from executive resolutions, as test questions frequently invert these two attributes.`,
    keyFacts: [
      'Statutory bodies derive jurisdiction from explicit Acts of Parliament.',
      'Constitutional bodies are directly mentioned in specific Articles of the Indian Constitution.',
      'Executive bodies are created via Government Resolutions without legislative enactment.',
    ],
    questions: generateFallbackQuestions('UPSC', 3, 'Medium'),
    previousAccuracy,
    currentAccuracy: 0.0,
    masteryUpdated: false,
  };
}

module.exports = {
  callGroqChat,
  analyzeNews,
  generateKnowledgeMap,
  generateQuestions,
  explainQuestion,
  generateRevision,
};
