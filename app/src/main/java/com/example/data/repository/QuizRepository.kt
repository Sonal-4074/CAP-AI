package com.example.data.repository

import android.util.Log
import com.example.data.local.CapDao
import com.example.data.local.QuizAttemptEntity
import com.example.data.model.ExamQuestion
import com.example.data.model.QuizAttempt
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "QuizRepository"

class QuizRepository(
    private val dao: CapDao,
    private val firestore: FirebaseFirestore
) {
    private val auth = Firebase.auth

    val localAttempts: Flow<List<QuizAttempt>> = dao.getAllAttempts().map { list ->
        list.map { entity ->
            QuizAttempt(
                id = entity.id,
                userId = auth.currentUser?.uid ?: "",
                quizType = entity.quizType,
                topic = entity.topic,
                targetExamination = entity.targetExamination,
                totalQuestions = entity.totalQuestions.toLong(),
                attemptedQuestions = entity.attemptedQuestions.toLong(),
                correctAnswers = entity.correctAnswers.toLong(),
                incorrectAnswers = entity.incorrectAnswers.toLong(),
                unattemptedQuestions = entity.unattemptedQuestions.toLong(),
                accuracy = entity.accuracy,
                timeTakenSeconds = entity.timeTakenSeconds,
                score = entity.score,
                completedAt = null
            )
        }
    }

    suspend fun recordQuizAttempt(
        quizType: String,
        topic: String,
        targetExamination: String,
        totalQuestions: Int,
        attemptedQuestions: Int,
        correctAnswers: Int,
        timeTakenSeconds: Long
    ): QuizAttempt = withContext(Dispatchers.IO) {
        val attemptId = UUID.randomUUID().toString()
        val incorrect = attemptedQuestions - correctAnswers
        val unattempted = totalQuestions - attemptedQuestions
        val accuracy = if (attemptedQuestions > 0) (correctAnswers.toDouble() / attemptedQuestions.toDouble()) * 100.0 else 0.0
        val score = correctAnswers.toDouble() - (incorrect.toDouble() * 0.25) // Standard negative marking (1/4th)

        val entity = QuizAttemptEntity(
            id = attemptId,
            quizType = quizType,
            topic = topic,
            targetExamination = targetExamination,
            totalQuestions = totalQuestions,
            attemptedQuestions = attemptedQuestions,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrect,
            unattemptedQuestions = unattempted,
            accuracy = accuracy,
            timeTakenSeconds = timeTakenSeconds,
            score = score,
            timestamp = System.currentTimeMillis()
        )
        dao.insertAttempt(entity)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("quizAttempts")
                    .document(attemptId)

                val payload = mapOf(
                    "userId" to uid,
                    "quizType" to quizType,
                    "topic" to topic,
                    "targetExamination" to targetExamination,
                    "totalQuestions" to totalQuestions.toLong(),
                    "attemptedQuestions" to attemptedQuestions.toLong(),
                    "correctAnswers" to correctAnswers.toLong(),
                    "incorrectAnswers" to incorrect.toLong(),
                    "unattemptedQuestions" to unattempted.toLong(),
                    "accuracy" to accuracy,
                    "timeTakenSeconds" to timeTakenSeconds,
                    "score" to score,
                    "completedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing quiz attempt to Firestore: ${e.message}")
            }
        }

        QuizAttempt(
            id = attemptId,
            userId = uid ?: "",
            quizType = quizType,
            topic = topic,
            targetExamination = targetExamination,
            totalQuestions = totalQuestions.toLong(),
            attemptedQuestions = attemptedQuestions.toLong(),
            correctAnswers = correctAnswers.toLong(),
            incorrectAnswers = incorrect.toLong(),
            unattemptedQuestions = unattempted.toLong(),
            accuracy = accuracy,
            timeTakenSeconds = timeTakenSeconds,
            score = score,
            completedAt = null
        )
    }
}
