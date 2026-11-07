package com.example.data.remote

import android.util.Log
import com.example.data.model.AiAnalysis
import com.example.data.model.Article
import com.example.data.model.ExamQuestion
import com.example.data.model.KeywordDefinition
import com.example.data.model.KnowledgeMapData
import com.example.data.model.MapEdge
import com.example.data.model.MapNode
import com.example.data.model.RevisionSession
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val TAG = "GeminiAiService"

class GeminiAiService {

    // Lazy initialization of Gemini model to prevent early class loading before Firebase is configured
    private val standardModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    private val textModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun analyzeNews(article: Article): Result<AiAnalysis> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a senior educator and exam analyst specializing in Indian competitive examinations (UPSC CSE, SSC CGL, Banking IBPS/SBI, State PSCs).
                Analyze the following current affairs news article thoroughly and provide the output in strict JSON format:
                
                Title: ${article.title}
                Category: ${article.category}
                Content: ${article.content}
                
                Required JSON structure:
                {
                  "simpleExplanation": "Clear, accessible explanation of the news in 2-3 sentences without bureaucratic jargon.",
                  "keyFacts": ["Fact 1 (dates/numbers/names)", "Fact 2", "Fact 3", "Fact 4"],
                  "examRelevance": "Detailed explanation of why this matters for exams (GS Papers for UPSC, General Awareness for SSC/Banking, related syllabus topics).",
                  "keywords": [
                    {"term": "Keyword 1", "definition": "Short concise exam definition."},
                    {"term": "Keyword 2", "definition": "Short concise exam definition."}
                  ],
                  "backgroundInfo": "Historical or policy background context to understand the root of this current event.",
                  "possibleExamQuestions": [
                    "Probable Prelims MCQ or Mains question 1",
                    "Probable question 2"
                  ],
                  "quickRevision": "A high-yield 2-sentence summary for last-minute revision."
                }
            """.trimIndent()

            val response = withTimeout(60_000L) {
                standardModel.generateContent(prompt)
            }

            val text = response.text ?: throw IllegalStateException("Model returned empty response.")
            val json = JSONObject(cleanJson(text))

            val keyFacts = mutableListOf<String>()
            val keyFactsArr = json.optJSONArray("keyFacts") ?: JSONArray()
            for (i in 0 until keyFactsArr.length()) {
                keyFacts.add(keyFactsArr.getString(i))
            }

            val keywords = mutableListOf<KeywordDefinition>()
            val keywordsArr = json.optJSONArray("keywords") ?: JSONArray()
            for (i in 0 until keywordsArr.length()) {
                val obj = keywordsArr.getJSONObject(i)
                keywords.add(
                    KeywordDefinition(
                        term = obj.optString("term", "Term"),
                        definition = obj.optString("definition", "")
                    )
                )
            }

            val questions = mutableListOf<String>()
            val questionsArr = json.optJSONArray("possibleExamQuestions") ?: JSONArray()
            for (i in 0 until questionsArr.length()) {
                questions.add(questionsArr.getString(i))
            }

            val analysis = AiAnalysis(
                articleId = article.id,
                simpleExplanation = json.optString("simpleExplanation", "Analysis generated successfully."),
                keyFacts = keyFacts,
                examRelevance = json.optString("examRelevance", "High relevance across General Studies."),
                keywords = keywords,
                backgroundInfo = json.optString("backgroundInfo", "Background context established."),
                possibleExamQuestions = questions,
                quickRevision = json.optString("quickRevision", "Quick revision snapshot.")
            )

            Result.success(analysis)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "analyzeNews failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateKnowledgeMap(topic: String, contextText: String): Result<KnowledgeMapData> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Create an interactive knowledge mind map for competitive exam preparation based on this topic:
                Topic: $topic
                Context: $contextText
                
                Identify the root main topic, key entities, constitutional/legal provisions, concepts, organizations, policies, and facts.
                Return strict JSON formatted as:
                {
                  "mainTopic": "$topic",
                  "nodes": [
                    {"id": "1", "label": "Main Headline/Concept", "type": "concept", "description": "Core subject description", "parentId": null},
                    {"id": "2", "label": "Key Entity/Body", "type": "organization", "description": "Responsible organization or constitutional body", "parentId": "1"},
                    {"id": "3", "label": "Policy/Scheme/Law", "type": "policy", "description": "Applicable legislation or government scheme", "parentId": "1"},
                    {"id": "4", "label": "Key Milestone/Event", "type": "event", "description": "Timeline date or event significance", "parentId": "2"},
                    {"id": "5", "label": "Static GK Connection", "type": "fact", "description": "Constitutional Article or fundamental concept", "parentId": "3"}
                  ],
                  "relationships": [
                    {"fromId": "1", "toId": "2", "label": "Administered by"},
                    {"fromId": "1", "toId": "3", "label": "Governed by"},
                    {"fromId": "2", "toId": "4", "label": "Conducted"},
                    {"fromId": "3", "toId": "5", "label": "Derived from"}
                  ]
                }
            """.trimIndent()

            val response = withTimeout(60_000L) {
                standardModel.generateContent(prompt)
            }

            val text = response.text ?: throw IllegalStateException("Model returned empty response.")
            val json = JSONObject(cleanJson(text))

            val nodesList = mutableListOf<MapNode>()
            val nodesArr = json.optJSONArray("nodes") ?: JSONArray()
            for (i in 0 until nodesArr.length()) {
                val nodeObj = nodesArr.getJSONObject(i)
                nodesList.add(
                    MapNode(
                        id = nodeObj.optString("id", UUID.randomUUID().toString()),
                        label = nodeObj.optString("label", "Node"),
                        type = nodeObj.optString("type", "concept"),
                        description = nodeObj.optString("description", ""),
                        parentId = if (nodeObj.isNull("parentId")) null else nodeObj.optString("parentId")
                    )
                )
            }

            val edgesList = mutableListOf<MapEdge>()
            val edgesArr = json.optJSONArray("relationships") ?: JSONArray()
            for (i in 0 until edgesArr.length()) {
                val edgeObj = edgesArr.getJSONObject(i)
                edgesList.add(
                    MapEdge(
                        fromId = edgeObj.optString("fromId", "1"),
                        toId = edgeObj.optString("toId", "2"),
                        label = edgeObj.optString("label", "Related to")
                    )
                )
            }

            val mapData = KnowledgeMapData(
                mainTopic = json.optString("mainTopic", topic),
                nodes = nodesList,
                relationships = edgesList
            )

            Result.success(mapData)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "generateKnowledgeMap failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuestions(
        articleText: String,
        targetExam: String,
        count: Int,
        difficulty: String
    ): Result<List<ExamQuestion>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are an exam setter for Indian competitive exams ($targetExam).
                Generate exactly $count high-quality multiple choice questions based on this current affairs content:
                
                Content: $articleText
                Exam Style: $targetExam
                Difficulty: $difficulty
                
                Focus:
                - If UPSC: Multi-statement, analytical concepts, government policies, reports, constitutional links.
                - If Banking (IBPS/SBI): RBI regulations, monetary policy, fiscal numbers, fintech, government schemes.
                - If SSC CGL: Appointments, summits, sports, awards, science discoveries, static GK connections.
                
                Return a strict JSON array of objects with format:
                [
                  {
                    "question": "Question text?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctIndex": 0,
                    "explanation": "Detailed pedagogical explanation explaining why the correct answer is right and why distractors are wrong.",
                    "difficulty": "$difficulty",
                    "category": "Current Affairs",
                    "relatedConcept": "Key Concept"
                  }
                ]
            """.trimIndent()

            val response = withTimeout(60_000L) {
                standardModel.generateContent(prompt)
            }

            val text = response.text ?: throw IllegalStateException("Model returned empty response.")
            val jsonArray = JSONArray(cleanJson(text))
            val questions = mutableListOf<ExamQuestion>()

            for (i in 0 until jsonArray.length()) {
                val qObj = jsonArray.getJSONObject(i)
                val optArr = qObj.getJSONArray("options")
                val options = (0 until optArr.length()).map { optArr.getString(it) }

                questions.add(
                    ExamQuestion(
                        id = UUID.randomUUID().toString(),
                        question = qObj.getString("question"),
                        options = options,
                        correctIndex = qObj.optInt("correctIndex", 0),
                        explanation = qObj.getString("explanation"),
                        difficulty = qObj.optString("difficulty", difficulty),
                        category = qObj.optString("category", "General Studies"),
                        relatedConcept = qObj.optString("relatedConcept", "Current Events"),
                        examType = targetExam
                    )
                )
            }

            Result.success(questions)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "generateQuestions failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateRevision(
        topic: String,
        weakConcepts: List<String>,
        previousAccuracy: Double
    ): Result<RevisionSession> = withContext(Dispatchers.IO) {
        try {
            val conceptsStr = if (weakConcepts.isEmpty()) "Core foundational concepts" else weakConcepts.joinToString(", ")
            val prompt = """
                You are an adaptive AI learning coach for Indian competitive exam aspirants.
                The student has scored ${previousAccuracy.toInt()}% accuracy in "$topic" with identified weak concepts: $conceptsStr.
                
                Generate a targeted personalized revision module in strict JSON format:
                {
                  "explanation": "Structured, crystal-clear conceptual explanation that directly resolves common student confusion in these weak concepts.",
                  "keyFacts": [
                    "High-yield fact 1 that frequently appears in exam papers",
                    "High-yield fact 2 with exact terms/figures",
                    "High-yield fact 3 with static GK connection",
                    "High-yield fact 4"
                  ],
                  "questions": [
                    {
                      "question": "Targeted test question 1 testing concept?",
                      "options": ["A", "B", "C", "D"],
                      "correctIndex": 0,
                      "explanation": "Clear explanation."
                    }
                  ]
                }
                Provide exactly 5 to 10 targeted test questions in the "questions" array.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                standardModel.generateContent(prompt)
            }

            val text = response.text ?: throw IllegalStateException("Model returned empty response.")
            val json = JSONObject(cleanJson(text))

            val facts = mutableListOf<String>()
            val factsArr = json.optJSONArray("keyFacts") ?: JSONArray()
            for (i in 0 until factsArr.length()) {
                facts.add(factsArr.getString(i))
            }

            val questions = mutableListOf<ExamQuestion>()
            val qArr = json.optJSONArray("questions") ?: JSONArray()
            for (i in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(i)
                val optArr = qObj.optJSONArray("options") ?: JSONArray()
                val options = (0 until optArr.length()).map { optArr.getString(it) }
                questions.add(
                    ExamQuestion(
                        id = UUID.randomUUID().toString(),
                        question = qObj.optString("question", "Question"),
                        options = options,
                        correctIndex = qObj.optInt("correctIndex", 0),
                        explanation = qObj.optString("explanation", "Explanation"),
                        difficulty = "Medium",
                        category = topic,
                        relatedConcept = conceptsStr,
                        examType = "Revision"
                    )
                )
            }

            val session = RevisionSession(
                topic = topic,
                concepts = weakConcepts,
                explanation = json.optString("explanation", "Comprehensive conceptual revision notes."),
                keyFacts = facts,
                questions = questions,
                previousAccuracy = previousAccuracy,
                currentAccuracy = 0.0,
                masteryUpdated = false
            )

            Result.success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "generateRevision failed", e)
            Result.failure(e)
        }
    }

    private fun cleanJson(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }
}
