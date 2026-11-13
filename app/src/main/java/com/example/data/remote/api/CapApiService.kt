package com.example.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface CapApiService {

    // Authentication Endpoints
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body req: VerifyEmailRequest): Response<AuthResponse>

    @POST("api/auth/resend-otp")
    suspend fun resendOtp(@Body req: ResendOtpRequest): Response<BaseResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): Response<BaseResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): Response<BaseResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<BaseResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body req: UpdateProfileRequest): Response<UserResponse>

    @DELETE("api/auth/account")
    suspend fun deleteAccount(): Response<BaseResponse>

    // Groq AI Endpoints
    @POST("api/ai/analyze-news")
    suspend fun analyzeNews(@Body req: AnalyzeNewsRequest): Response<AnalysisResponse>

    @POST("api/ai/knowledge-map")
    suspend fun generateKnowledgeMap(@Body req: KnowledgeMapRequest): Response<KnowledgeMapResponse>

    @POST("api/ai/generate-questions")
    suspend fun generateQuestions(@Body req: GenerateQuestionsRequest): Response<QuestionsResponse>

    @POST("api/ai/explain-question")
    suspend fun explainQuestion(@Body req: ExplainQuestionRequest): Response<BaseResponse>

    @POST("api/ai/revision")
    suspend fun generateRevision(@Body req: RevisionRequest): Response<RevisionResponse>

    // User-Specific Preparation Data
    @GET("api/user/preparation-data")
    suspend fun getPreparationData(): Response<PreparationDataResponse>

    @POST("api/user/quiz-attempt")
    suspend fun recordQuizAttempt(@Body req: QuizAttemptRequest): Response<BaseResponse>

    @POST("api/user/bookmarks")
    suspend fun toggleBookmark(@Body req: BookmarkRequest): Response<BaseResponse>
}
