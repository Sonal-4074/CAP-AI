package com.example.data.repository

import android.util.Log
import com.example.data.local.CapDao
import com.example.data.local.PerformanceEntity
import com.example.data.model.TopicPerformance
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray

private const val TAG = "PerformanceRepository"

class PerformanceRepository(
    private val dao: CapDao,
    private val firestore: FirebaseFirestore
) {
    private val auth = Firebase.auth

    val performance: Flow<List<TopicPerformance>> = dao.getAllPerformance().map { entities ->
        entities.map { entity ->
            val concepts = try {
                if (entity.weakConceptsJson.isNotBlank()) {
                    val arr = JSONArray(entity.weakConceptsJson)
                    (0 until arr.length()).map { arr.getString(it) }
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            TopicPerformance(
                id = entity.topic,
                userId = auth.currentUser?.uid ?: "",
                topic = entity.topic,
                attempted = entity.attempted.toLong(),
                correct = entity.correct.toLong(),
                incorrect = entity.incorrect.toLong(),
                accuracy = entity.accuracy,
                masteryLevel = entity.masteryLevel,
                weakConcepts = concepts,
                repeatedMistakes = entity.repeatedMistakes.toLong(),
                updatedAt = null
            )
        }
    }

    suspend fun updatePerformanceForAttempt(
        topic: String,
        attempted: Int,
        correct: Int,
        weakConceptsToAdd: List<String> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getPerformanceForTopic(topic)

        val totalAttempted = (existing?.attempted ?: 0) + attempted
        val totalCorrect = (existing?.correct ?: 0) + correct
        val totalIncorrect = totalAttempted - totalCorrect
        val accuracy = if (totalAttempted > 0) (totalCorrect.toDouble() / totalAttempted.toDouble()) * 100.0 else 0.0

        // Strict Indian Exam Mastery Classification:
        // Below 50%: Weak
        // 50–69%: Needs Improvement
        // 70–84%: Good
        // 85% and Above: Strong
        val mastery = when {
            accuracy < 50.0 -> "Weak"
            accuracy < 70.0 -> "Needs Improvement"
            accuracy < 85.0 -> "Good"
            else -> "Strong"
        }

        val existingConcepts = try {
            if (existing != null && existing.weakConceptsJson.isNotBlank()) {
                val arr = JSONArray(existing.weakConceptsJson)
                (0 until arr.length()).map { arr.getString(it) }.toMutableSet()
            } else mutableSetOf()
        } catch (e: Exception) {
            mutableSetOf()
        }

        if (mastery == "Weak" || mastery == "Needs Improvement") {
            existingConcepts.addAll(weakConceptsToAdd)
        } else if (mastery == "Strong") {
            existingConcepts.removeAll(weakConceptsToAdd.toSet())
        }

        val repeated = if (existing != null && mastery == "Weak") existing.repeatedMistakes + 1 else existing?.repeatedMistakes ?: 0

        val updatedEntity = PerformanceEntity(
            topic = topic,
            attempted = totalAttempted,
            correct = totalCorrect,
            incorrect = totalIncorrect,
            accuracy = accuracy,
            masteryLevel = mastery,
            weakConceptsJson = JSONArray(existingConcepts.toList()).toString(),
            repeatedMistakes = repeated,
            timestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdatePerformance(updatedEntity)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                // Topic ID safe for Firestore document path
                val topicDocId = topic.replace(" ", "_").replace("&", "and")
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("userPerformance")
                    .document(topicDocId)

                val payload = mapOf(
                    "userId" to uid,
                    "topic" to topic,
                    "attempted" to totalAttempted.toLong(),
                    "correct" to totalCorrect.toLong(),
                    "incorrect" to totalIncorrect.toLong(),
                    "accuracy" to accuracy,
                    "masteryLevel" to mastery,
                    "weakConcepts" to existingConcepts.toList(),
                    "repeatedMistakes" to repeated.toLong(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing performance to Firestore: ${e.message}")
            }
        }
    }

    suspend fun seedInitialPerformanceIfEmpty() = withContext(Dispatchers.IO) {
        val initialTopics = listOf(
            PerformanceEntity(
                topic = "Indian Economy",
                attempted = 25,
                correct = 11,
                incorrect = 14,
                accuracy = 44.0,
                masteryLevel = "Weak",
                weakConceptsJson = "[\"Repo Rate & Reverse Repo\", \"Liquidity Adjustment Facility\", \"Inflation Targeting 4% +/- 2%\"]",
                repeatedMistakes = 3,
                timestamp = System.currentTimeMillis()
            ),
            PerformanceEntity(
                topic = "Science and Technology",
                attempted = 30,
                correct = 26,
                incorrect = 4,
                accuracy = 86.7,
                masteryLevel = "Strong",
                weakConceptsJson = "[]",
                repeatedMistakes = 0,
                timestamp = System.currentTimeMillis()
            ),
            PerformanceEntity(
                topic = "Environment & Ecology",
                attempted = 20,
                correct = 12,
                incorrect = 8,
                accuracy = 60.0,
                masteryLevel = "Needs Improvement",
                weakConceptsJson = "[\"CBDR-RC Principle\", \"Loss and Damage Fund\"]",
                repeatedMistakes = 1,
                timestamp = System.currentTimeMillis()
            ),
            PerformanceEntity(
                topic = "Government Schemes",
                attempted = 15,
                correct = 11,
                incorrect = 4,
                accuracy = 73.3,
                masteryLevel = "Good",
                weakConceptsJson = "[\"Subsidy slabs for PM-Surya Ghar\"]",
                repeatedMistakes = 0,
                timestamp = System.currentTimeMillis()
            )
        )
        for (item in initialTopics) {
            dao.insertOrUpdatePerformance(item)
        }
    }
}
