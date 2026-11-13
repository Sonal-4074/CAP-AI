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
    val fullName: String = name,
    val email: String = "",
    val isEmailVerified: Boolean = true,
    val profilePicture: String = "",
    val targetExamination: String = "UPSC",
    val targetExaminations: List<String> = listOf(targetExamination),
    val dailyQuestionTarget: Long = 20L,
    val preferredDifficulty: String = "Medium",
    val difficultyLevel: String = preferredDifficulty,
    val studyStreak: Long = 1L,
    val preferredCategories: List<String> = listOf("National Affairs", "Economy", "Science and Technology"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val displayName: String
        get() = if (fullName.isNotBlank()) fullName else if (name.isNotBlank()) name else "Aspirant"
}
