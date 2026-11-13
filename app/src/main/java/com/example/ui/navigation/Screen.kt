package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Welcome : Screen("welcome")
    data object Register : Screen("register")
    data object EmailVerification : Screen("email_verification")
    data object Login : Screen("login")
    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password")
    data object ProfileSetup : Screen("profile_setup")
    data object Auth : Screen("auth") // Kept for backward compatibility
    data object MainShell : Screen("main_shell")
    data object NewsDetail : Screen("news_detail/{articleId}") {
        fun createRoute(articleId: String) = "news_detail/$articleId"
    }
    data object AiAnalysis : Screen("ai_analysis/{articleId}") {
        fun createRoute(articleId: String) = "ai_analysis/$articleId"
    }
    data object KnowledgeMapViewer : Screen("knowledge_map_viewer/{topic}") {
        fun createRoute(topic: String) = "knowledge_map_viewer/$topic"
    }
    data object QuizConfig : Screen("quiz_config")
    data object ActiveQuiz : Screen("active_quiz")
    data object QuizResult : Screen("quiz_result")
    data object PyqPractice : Screen("pyq_practice")
    data object RevisionDetail : Screen("revision_detail/{topic}") {
        fun createRoute(topic: String) = "revision_detail/$topic"
    }
    data object Bookmarks : Screen("bookmarks")
    data object Profile : Screen("profile")
}

enum class BottomTab(val route: String, val title: String) {
    DASHBOARD("dashboard", "Home"),
    NEWS("news", "Current Affairs"),
    KNOWLEDGE_MAP("maps", "Mind Maps"),
    PYQ("pyq", "PYQs"),
    ANALYTICS("analytics", "Analytics")
}
