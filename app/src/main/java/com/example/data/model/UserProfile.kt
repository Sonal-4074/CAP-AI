package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val targetExamination: String = "UPSC",
    val dailyQuestionTarget: Long = 20L,
    val preferredDifficulty: String = "Medium",
    val studyStreak: Long = 1L,
    val preferredCategories: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
