package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TopicPerformance(
    @DocumentId val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val attempted: Long = 0L,
    val correct: Long = 0L,
    val incorrect: Long = 0L,
    val accuracy: Double = 0.0,
    val masteryLevel: String = "Needs Improvement", // Weak, Needs Improvement, Good, Strong
    val weakConcepts: List<String> = emptyList(),
    val repeatedMistakes: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
