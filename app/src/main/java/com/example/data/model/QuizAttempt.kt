package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class QuizAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val quizType: String = "AI_QUIZ", // AI_QUIZ, PYQ, REVISION
    val topic: String = "",
    val targetExamination: String = "",
    val totalQuestions: Long = 0L,
    val attemptedQuestions: Long = 0L,
    val correctAnswers: Long = 0L,
    val incorrectAnswers: Long = 0L,
    val unattemptedQuestions: Long = 0L,
    val accuracy: Double = 0.0,
    val timeTakenSeconds: Long = 0L,
    val score: Double = 0.0,
    @ServerTimestamp val completedAt: Timestamp? = null
)
