package com.example.data.remote.api

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cap_ai_auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(loadSavedProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun saveAuth(token: String, user: UserProfile) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.userId)
            .putString(KEY_USER_NAME, user.fullName.ifBlank { user.name })
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_TARGET_EXAM, user.targetExamination)
            .putLong(KEY_DAILY_TARGET, user.dailyQuestionTarget)
            .putString(KEY_DIFFICULTY, user.preferredDifficulty)
            .apply()

        _currentUser.value = user
        _isLoggedIn.value = true
    }

    fun updateProfile(user: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_NAME, user.fullName.ifBlank { user.name })
            .putString(KEY_TARGET_EXAM, user.targetExamination)
            .putLong(KEY_DAILY_TARGET, user.dailyQuestionTarget)
            .putString(KEY_DIFFICULTY, user.preferredDifficulty)
            .apply()

        _currentUser.value = user
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun hasToken(): Boolean {
        return !prefs.getString(KEY_TOKEN, null).isNullOrBlank()
    }

    fun clearAuth() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    private fun loadSavedProfile(): UserProfile? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "Aspirant") ?: "Aspirant"
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val targetExam = prefs.getString(KEY_TARGET_EXAM, "UPSC") ?: "UPSC"
        val dailyTarget = prefs.getLong(KEY_DAILY_TARGET, 20L)
        val difficulty = prefs.getString(KEY_DIFFICULTY, "Medium") ?: "Medium"

        return UserProfile(
            userId = userId,
            name = name,
            fullName = name,
            email = email,
            targetExamination = targetExam,
            targetExaminations = listOf(targetExam),
            dailyQuestionTarget = dailyTarget,
            preferredDifficulty = difficulty,
            difficultyLevel = difficulty
        )
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_TARGET_EXAM = "target_exam"
        private const val KEY_DAILY_TARGET = "daily_target"
        private const val KEY_DIFFICULTY = "difficulty"
    }
}
