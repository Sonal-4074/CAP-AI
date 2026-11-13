package com.example.data.repository

import android.util.Log
import com.example.data.local.CapDao
import com.example.data.local.RevisionNoteEntity
import com.example.data.model.RevisionSession
import com.example.data.remote.GroqAiService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

private const val TAG = "RevisionRepository"

class RevisionRepository(
    private val dao: CapDao,
    private val firestore: FirebaseFirestore,
    private val aiService: GroqAiService,
    private val performanceRepo: PerformanceRepository
) {
    private val auth = Firebase.auth

    suspend fun startRevisionSession(
        topic: String,
        weakConcepts: List<String>,
        previousAccuracy: Double
    ): Result<RevisionSession> = withContext(Dispatchers.IO) {
        val result = aiService.generateRevision(topic, weakConcepts, previousAccuracy)
        if (result.isSuccess) {
            val session = result.getOrThrow()
            // Cache in Room
            val noteId = UUID.randomUUID().toString()
            val entity = RevisionNoteEntity(
                id = noteId,
                topic = session.topic,
                conceptsJson = JSONArray(session.concepts).toString(),
                summary = session.explanation,
                keyFactsJson = JSONArray(session.keyFacts).toString(),
                previousAccuracy = session.previousAccuracy,
                newAccuracy = 0.0,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            dao.insertRevisionNote(entity)

            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    firestore.collection("users")
                        .document(uid)
                        .collection("revisionNotes")
                        .document(noteId)
                        .set(
                            mapOf(
                                "userId" to uid,
                                "topic" to session.topic,
                                "concepts" to session.concepts,
                                "summary" to session.explanation,
                                "keyFacts" to session.keyFacts,
                                "previousAccuracy" to session.previousAccuracy,
                                "newAccuracy" to 0.0,
                                "status" to "Pending",
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving revision note to Firestore: ${e.message}")
                }
            }
        }
        result
    }

    suspend fun completeRevisionReTest(
        topic: String,
        totalQuestions: Int,
        correctAnswers: Int,
        previousAccuracy: Double
    ): Double = withContext(Dispatchers.IO) {
        val newAccuracy = if (totalQuestions > 0) (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100.0 else 0.0
        // Update topic mastery
        performanceRepo.updatePerformanceForAttempt(
            topic = topic,
            attempted = totalQuestions,
            correct = correctAnswers
        )
        newAccuracy
    }
}
