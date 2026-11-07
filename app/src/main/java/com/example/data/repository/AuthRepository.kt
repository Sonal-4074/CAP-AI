package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AuthRepository"

class AuthRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore
) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    fetchUserProfile(firebaseAuth.currentUser!!.uid)
                }
            } else {
                _userProfile.value = null
            }
        }
    }

    suspend fun attemptAutoSignIn(): Boolean = withContext(Dispatchers.IO) {
        if (auth.currentUser != null) {
            fetchUserProfile(auth.currentUser!!.uid)
            return@withContext true
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "Web client ID not found: ${e.message}")
            return@withContext false
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                authResult.user?.let { user ->
                    fetchUserProfile(user.uid)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "Silent auto sign-in not available: ${e.message}")
            false
        }
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                authResult.user?.let { user ->
                    syncUserProfileOnLogin(user)
                }
                onSuccess()
            } else {
                onError("Unexpected credential format received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled: ${e.message}")
            onCancelled()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign-in failed.")
        }
    }

    suspend fun signOut(onComplete: () -> Unit) = withContext(Dispatchers.IO) {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed clearing credential state: ${e.message}")
            } finally {
                auth.signOut()
                _userProfile.value = null
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    private suspend fun syncUserProfileOnLogin(user: FirebaseUser) = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection("users").document(user.uid)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                val initialProfile = mapOf(
                    "userId" to user.uid,
                    "name" to (user.displayName ?: "Aspirant"),
                    "email" to (user.email ?: "user@capai.edu"),
                    "targetExamination" to "UPSC",
                    "dailyQuestionTarget" to 20L,
                    "preferredDifficulty" to "Medium",
                    "studyStreak" to 1L,
                    "preferredCategories" to listOf("National Affairs", "Economy", "Science and Technology"),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(initialProfile).await()
            }
            fetchUserProfile(user.uid)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile", e)
        }
    }

    suspend fun fetchUserProfile(uid: String) = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                _userProfile.value = profile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile", e)
        }
    }

    suspend fun updateTargetExam(exam: String) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "targetExamination" to exam,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            fetchUserProfile(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update target exam", e)
        }
    }

    suspend fun updateDailyGoal(target: Long) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "dailyQuestionTarget" to target,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            fetchUserProfile(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update daily goal", e)
        }
    }
}
