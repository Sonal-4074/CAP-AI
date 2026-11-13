package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.UnverifiedEmailException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class CodeSent(val email: String, val message: String, val debugOtp: String? = null) : AuthUiState
    data class Success(val user: UserProfile, val message: String) : AuthUiState
    data class PasswordResetSuccess(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Resend OTP countdown timer
    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()
    private var timerJob: Job? = null

    // Transient state across auth screens
    var pendingEmail: String = ""
        private set
    var pendingDebugOtp: String? = null
        private set

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun setPendingEmail(email: String) {
        pendingEmail = email.trim()
    }

    fun startCountdown(seconds: Int = 60) {
        timerJob?.cancel()
        _resendCountdown.value = seconds
        timerJob = viewModelScope.launch {
            while (_resendCountdown.value > 0) {
                delay(1000)
                _resendCountdown.value -= 1
            }
        }
    }

    // 1. Register
    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (email: String, debugOtp: String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.register(fullName, email, password, confirmPassword)
            result.onSuccess { data ->
                pendingEmail = data.email
                pendingDebugOtp = data.debugOtp
                _uiState.value = AuthUiState.CodeSent(data.email, data.message, data.debugOtp)
                startCountdown(60)
                onSuccess(data.email, data.debugOtp)
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Registration failed.")
            }
        }
    }

    // 2. Verify Email OTP
    fun verifyEmail(
        email: String,
        otp: String,
        onSuccess: (UserProfile) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.verifyEmail(email, otp)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user, "Email verified successfully.")
                onSuccess(user)
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Verification failed.")
            }
        }
    }

    // 3. Resend OTP
    fun resendOtp(email: String) {
        if (_resendCountdown.value > 0) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.resendOtp(email)
            result.onSuccess { msg ->
                _uiState.value = AuthUiState.CodeSent(email, msg)
                startCountdown(60)
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Could not resend OTP.")
            }
        }
    }

    // 4. Login
    fun login(
        email: String,
        password: String,
        onSuccess: (UserProfile) -> Unit,
        onUnverified: (email: String, debugOtp: String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.login(email, password)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user, "Login successful.")
                onSuccess(user)
            }.onFailure { err ->
                if (err is UnverifiedEmailException) {
                    pendingEmail = err.email
                    pendingDebugOtp = err.debugOtp
                    _uiState.value = AuthUiState.CodeSent(err.email, err.message, err.debugOtp)
                    startCountdown(60)
                    onUnverified(err.email, err.debugOtp)
                } else {
                    _uiState.value = AuthUiState.Error(err.message ?: "Invalid email or password.")
                }
            }
        }
    }

    // 5. Forgot Password
    fun forgotPassword(
        email: String,
        onSuccess: (email: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.forgotPassword(email)
            result.onSuccess { msg ->
                pendingEmail = email.trim()
                _uiState.value = AuthUiState.CodeSent(email, msg)
                startCountdown(60)
                onSuccess(email.trim())
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Failed to send reset code.")
            }
        }
    }

    // 6. Reset Password
    fun resetPassword(
        email: String,
        otp: String,
        newPassword: String,
        confirmNewPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.resetPassword(email, otp, newPassword, confirmNewPassword)
            result.onSuccess { msg ->
                _uiState.value = AuthUiState.PasswordResetSuccess(msg)
                onSuccess()
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Password reset failed.")
            }
        }
    }

    // 7. Update User Profile
    fun updateProfile(
        fullName: String? = null,
        targetExaminations: List<String>? = null,
        preferredCategories: List<String>? = null,
        difficultyLevel: String? = null,
        dailyQuestionTarget: Int? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.updateProfile(
                fullName = fullName,
                targetExaminations = targetExaminations,
                preferredCategories = preferredCategories,
                difficultyLevel = difficultyLevel,
                dailyQuestionTarget = dailyQuestionTarget
            )
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user, "Profile updated.")
                onSuccess()
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Failed to update profile.")
            }
        }
    }

    // 8. Sign Out
    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.signOut {
                _uiState.value = AuthUiState.Idle
                onComplete()
            }
        }
    }

    // 9. Delete Account
    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.deleteAccount()
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
