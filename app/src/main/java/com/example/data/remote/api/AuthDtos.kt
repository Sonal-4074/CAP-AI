package com.example.data.remote.api

import com.example.data.model.AiAnalysis
import com.example.data.model.Article
import com.example.data.model.ExamQuestion
import com.example.data.model.KnowledgeMapData
import com.example.data.model.RevisionSession
import com.example.data.model.TopicPerformance
import com.example.data.model.UserProfile

// Base & Generic Responses
data class BaseResponse(
    val success: Boolean,
    val message: String? = null,
    val email: String? = null,
    val debugOtp: String? = null
)

// Auth Requests
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class VerifyEmailRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String,
    val confirmNewPassword: String
)

data class UpdateProfileRequest(
    val fullName: String? = null,
    val profilePicture: String? = null,
    val targetExaminations: List<String>? = null,
    val preferredCategories: List<String>? = null,
    val difficultyLevel: String? = null,
    val dailyQuestionTarget: Int? = null
)

// Auth Responses
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserDto? = null,
    val email: String? = null,
    val debugOtp: String? = null,
    val isEmailVerified: Boolean = true
)

data class UserResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val _id: String = "",
    val fullName: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false,
    val profilePicture: String = "",
    val targetExaminations: List<String> = emptyList(),
    val preferredCategories: List<String> = emptyList(),
    val difficultyLevel: String = "Medium",
    val dailyQuestionTarget: Long = 20L
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            userId = _id,
            name = fullName,
            fullName = fullName,
            email = email,
            isEmailVerified = isEmailVerified,
            profilePicture = profilePicture,
            targetExamination = targetExaminations.firstOrNull() ?: "UPSC",
            targetExaminations = if (targetExaminations.isNotEmpty()) targetExaminations else listOf("UPSC"),
            dailyQuestionTarget = dailyQuestionTarget,
            preferredDifficulty = difficultyLevel,
            difficultyLevel = difficultyLevel,
            preferredCategories = preferredCategories
        )
    }
}

// Preparation Data Response
data class PreparationDataResponse(
    val success: Boolean,
    val data: PreparationDataDto? = null,
    val message: String? = null
)

data class PreparationDataDto(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val targetExaminations: List<String> = emptyList(),
    val dailyQuestionTarget: Long = 20L,
    val difficultyLevel: String = "Medium",
    val totalAttempted: Long = 0L,
    val totalCorrect: Long = 0L,
    val overallAccuracy: Double = 0.0,
    val weakTopics: List<String> = emptyList(),
    val bookmarksCount: Int = 0,
    val articlesReadCount: Int = 0
)

data class QuizAttemptRequest(
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val examType: String = "General"
)

data class BookmarkRequest(
    val articleId: String,
    val title: String,
    val category: String
)

// AI Requests
data class AnalyzeNewsRequest(
    val article: Article
)

data class AnalysisResponse(
    val success: Boolean,
    val data: AiAnalysis? = null,
    val message: String? = null
)

data class KnowledgeMapRequest(
    val topic: String,
    val contextText: String = ""
)

data class KnowledgeMapResponse(
    val success: Boolean,
    val data: KnowledgeMapData? = null,
    val message: String? = null
)

data class GenerateQuestionsRequest(
    val articleText: String,
    val targetExam: String = "UPSC",
    val count: Int = 5,
    val difficulty: String = "Medium"
)

data class QuestionsResponse(
    val success: Boolean,
    val data: List<ExamQuestion>? = null,
    val message: String? = null
)

data class ExplainQuestionRequest(
    val question: String,
    val options: List<String>,
    val selectedOption: String,
    val correctOption: String
)

data class RevisionRequest(
    val topic: String,
    val weakConcepts: List<String> = emptyList(),
    val previousAccuracy: Double = 45.0
)

data class RevisionResponse(
    val success: Boolean,
    val data: RevisionSession? = null,
    val message: String? = null
)
