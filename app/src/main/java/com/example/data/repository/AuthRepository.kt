package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.model.UserProfile
import com.example.data.remote.api.ApiClient
import com.example.data.remote.api.CapApiService
import com.example.data.remote.api.ForgotPasswordRequest
import com.example.data.remote.api.LoginRequest
import com.example.data.remote.api.RegisterRequest
import com.example.data.remote.api.ResendOtpRequest
import com.example.data.remote.api.ResetPasswordRequest
import com.example.data.remote.api.TokenManager
import com.example.data.remote.api.UpdateProfileRequest
import com.example.data.remote.api.VerifyEmailRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val TAG = "AuthRepository"

class AuthRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore? = null,
    val tokenManager: TokenManager = TokenManager(context)
) {
    private val apiService: CapApiService = ApiClient.getService(context, tokenManager)
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    // Observable authentication state
    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn
    val userProfile: StateFlow<UserProfile?> = tokenManager.currentUser

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    suspend fun checkAuthStatus(): Boolean = withContext(Dispatchers.IO) {
        if (tokenManager.hasToken()) {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful && response.body()?.user != null) {
                    val profile = response.body()!!.user!!.toUserProfile()
                    tokenManager.updateProfile(profile)
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Offline or backend unreachable, using locally cached profile: ${e.message}")
                return@withContext tokenManager.hasToken()
            }
        }
        return@withContext false
    }

    // 1. User Registration
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult> = withContext(Dispatchers.IO) {
        try {
            val req = RegisterRequest(
                fullName = fullName.trim(),
                email = email.trim(),
                password = password,
                confirmPassword = confirmPassword
            )
            val response = apiService.register(req)

            if (response.isSuccessful) {
                val body = response.body()
                val debugOtp = body?.debugOtp
                val message = body?.message ?: "Verification OTP sent to your email."
                Result.success(RegisterResult(email = email.trim(), debugOtp = debugOtp, message = message))
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Registration failed. Please verify your details.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration network error", e)
            Result.failure(Exception(formatNetworkError(e, "Unable to connect to CAP AI server.")))
        }
    }

    // 2. Email Verification via OTP
    suspend fun verifyEmail(email: String, otp: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val req = VerifyEmailRequest(email = email.trim(), otp = otp.trim())
            val response = apiService.verifyEmail(req)

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                val userDto = body?.user
                if (token != null && userDto != null) {
                    val profile = userDto.toUserProfile()
                    tokenManager.saveAuth(token, profile)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Verification succeeded but no session token received."))
                }
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Verification failed. Please check your OTP.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email verification error", e)
            Result.failure(Exception(formatNetworkError(e, "Failed to verify email. Please try again.")))
        }
    }

    // 3. Resend OTP
    suspend fun resendOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resendOtp(ResendOtpRequest(email.trim()))
            if (response.isSuccessful) {
                val debugOtp = response.body()?.debugOtp
                val msg = response.body()?.message ?: "New OTP sent."
                Result.success(if (debugOtp != null) "$msg (OTP: $debugOtp)" else msg)
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Failed to resend OTP.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resend OTP error", e)
            Result.failure(Exception(formatNetworkError(e, "Network error while resending OTP.")))
        }
    }

    // 4. User Login
    suspend fun login(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val req = LoginRequest(email = email.trim(), password = password)
            val response = apiService.login(req)

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                val userDto = body?.user

                if (token != null && userDto != null) {
                    val profile = userDto.toUserProfile()
                    tokenManager.saveAuth(token, profile)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Login succeeded but session data was incomplete."))
                }
            } else {
                val errJson = response.errorBody()?.string()
                // Check if unverified
                if (response.code() == 403) {
                    val debugOtp = try {
                        val json = JSONObject(errJson ?: "{}")
                        if (json.has("debugOtp") && !json.isNull("debugOtp")) json.getString("debugOtp") else null
                    } catch (e: Exception) { null }
                    return@withContext Result.failure(
                        UnverifiedEmailException(
                            email = email.trim(),
                            debugOtp = debugOtp,
                            message = "Email is not verified yet. An OTP has been sent."
                        )
                    )
                }
                val msg = parseErrorMessage(errJson, "Invalid email or password.")
                Result.failure(Exception(msg))
            }
        } catch (e: UnverifiedEmailException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(Exception(formatNetworkError(e, "Unable to connect to CAP AI server.")))
        }
    }

    // 5. Forgot Password
    suspend fun forgotPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
            if (response.isSuccessful) {
                val debugOtp = response.body()?.debugOtp
                val msg = response.body()?.message ?: "Password reset code sent."
                Result.success(if (debugOtp != null) "$msg (OTP: $debugOtp)" else msg)
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Failed to send reset code.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Forgot password error", e)
            Result.failure(Exception(formatNetworkError(e, "Network error while requesting password reset.")))
        }
    }

    // 6. Reset Password
    suspend fun resetPassword(
        email: String,
        otp: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = ResetPasswordRequest(
                email = email.trim(),
                otp = otp.trim(),
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword
            )
            val response = apiService.resetPassword(req)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password reset successfully. Please log in.")
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Password reset failed.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Reset password error", e)
            Result.failure(Exception(formatNetworkError(e, "Failed to reset password.")))
        }
    }

    // 7. Update User Profile
    suspend fun updateProfile(
        fullName: String? = null,
        targetExaminations: List<String>? = null,
        preferredCategories: List<String>? = null,
        difficultyLevel: String? = null,
        dailyQuestionTarget: Int? = null
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val req = UpdateProfileRequest(
                fullName = fullName,
                targetExaminations = targetExaminations,
                preferredCategories = preferredCategories,
                difficultyLevel = difficultyLevel,
                dailyQuestionTarget = dailyQuestionTarget
            )
            val response = apiService.updateProfile(req)
            if (response.isSuccessful && response.body()?.user != null) {
                val profile = response.body()!!.user!!.toUserProfile()
                tokenManager.updateProfile(profile)
                Result.success(profile)
            } else {
                val errJson = response.errorBody()?.string()
                val msg = parseErrorMessage(errJson, "Failed to update profile.")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update profile error", e)
            // Local fallback update
            val current = tokenManager.currentUser.value
            if (current != null) {
                val updated = current.copy(
                    fullName = fullName ?: current.fullName,
                    targetExamination = targetExaminations?.firstOrNull() ?: current.targetExamination,
                    targetExaminations = targetExaminations ?: current.targetExaminations,
                    preferredCategories = preferredCategories ?: current.preferredCategories,
                    preferredDifficulty = difficultyLevel ?: current.preferredDifficulty,
                    difficultyLevel = difficultyLevel ?: current.difficultyLevel,
                    dailyQuestionTarget = dailyQuestionTarget?.toLong() ?: current.dailyQuestionTarget
                )
                tokenManager.updateProfile(updated)
                Result.success(updated)
            } else {
                Result.failure(Exception("No active session to update."))
            }
        }
    }

    // 8. Delete Account
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteAccount()
            tokenManager.clearAuth()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearAuth()
            Result.success(Unit)
        }
    }

    // 9. Sign Out
    suspend fun signOut(onComplete: () -> Unit = {}) = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
        } catch (e: Exception) {
            Log.d(TAG, "Backend logout call error: ${e.message}")
        } finally {
            tokenManager.clearAuth()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // Compatibility methods for existing codebase
    suspend fun attemptAutoSignIn(): Boolean = withContext(Dispatchers.IO) {
        checkAuthStatus()
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        if (tokenManager.hasToken()) {
            onSuccess()
        } else {
            onCancelled()
        }
    }

    suspend fun updateTargetExam(exam: String) {
        updateProfile(targetExaminations = listOf(exam))
    }

    suspend fun updateDailyGoal(target: Long) {
        updateProfile(dailyQuestionTarget = target.toInt())
    }

    private fun parseErrorMessage(errJson: String?, defaultMsg: String): String {
        if (errJson.isNullOrBlank()) return defaultMsg
        return try {
            val obj = JSONObject(errJson)
            obj.optString("message", defaultMsg)
        } catch (e: Exception) {
            defaultMsg
        }
    }

    private fun formatNetworkError(e: Exception, defaultMsg: String): String {
        val msg = e.localizedMessage ?: ""
        return if (e is java.net.ConnectException || msg.contains("Failed to connect") || msg.contains("Connection refused")) {
            "Unable to connect to CAP AI backend on port 5000. Please ensure the Node.js server is running."
        } else if (e is java.net.SocketTimeoutException) {
            "Request timed out. Please check your network and try again."
        } else {
            e.localizedMessage ?: defaultMsg
        }
    }
}

data class RegisterResult(
    val email: String,
    val debugOtp: String?,
    val message: String
)

class UnverifiedEmailException(
    val email: String,
    val debugOtp: String?,
    override val message: String
) : Exception(message)
