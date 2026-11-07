package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.remote.GeminiAiService
import com.example.data.repository.AuthRepository
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.PerformanceRepository
import com.example.data.repository.PyqRepository
import com.example.data.repository.QuizRepository
import com.example.data.repository.RevisionRepository
import com.example.ui.screens.analysis.AiAnalysisScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.bookmarks.BookmarksScreen
import com.example.ui.screens.knowledgemap.KnowledgeMapScreen
import com.example.ui.screens.mainshell.MainShellScreen
import com.example.ui.screens.news.NewsDetailScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.quiz.ActiveQuizScreen
import com.example.ui.screens.quiz.QuizConfigScreen
import com.example.ui.screens.quiz.QuizResultScreen
import com.example.ui.screens.revision.RevisionScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.viewmodel.AnalysisViewModel
import com.example.ui.viewmodel.KnowledgeMapViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NewsViewModel
import com.example.ui.viewmodel.PerformanceViewModel
import com.example.ui.viewmodel.PyqViewModel
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.RevisionViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Single provisioned database ID from firebase_applet_config.xml
    val dbId = remember {
        val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else "ai-studio-android-capai-6a66acad-32ef-4aeb-a2ed-54123f94e883"
    }

    val firestore = remember {
        FirebaseFirestore.getInstance(dbId)
    }

    val database = remember { AppDatabase.getInstance(context) }
    val dao = remember { database.capDao() }

    val aiService = remember { GeminiAiService() }
    val authRepo = remember { AuthRepository(context, firestore) }
    val newsRepo = remember { NewsRepository(dao) }
    val bookmarkRepo = remember { BookmarkRepository(dao, firestore) }
    val quizRepo = remember { QuizRepository(dao, firestore) }
    val performanceRepo = remember { PerformanceRepository(dao, firestore) }
    val pyqRepo = remember { PyqRepository() }
    val revisionRepo = remember { RevisionRepository(dao, firestore, aiService, performanceRepo) }

    val mainViewModel = remember { MainViewModel(authRepo, newsRepo, performanceRepo) }
    val newsViewModel = remember { NewsViewModel(newsRepo, bookmarkRepo) }
    val performanceViewModel = remember { PerformanceViewModel(performanceRepo, quizRepo) }
    val knowledgeMapViewModel = remember { KnowledgeMapViewModel(aiService, bookmarkRepo) }
    val pyqViewModel = remember { PyqViewModel(pyqRepo, quizRepo, performanceRepo) }
    val analysisViewModel = remember { AnalysisViewModel(aiService) }
    val quizViewModel = remember { QuizViewModel(aiService, quizRepo, performanceRepo) }
    val revisionViewModel = remember { RevisionViewModel(revisionRepo) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = { selectedExam ->
                    mainViewModel.updateTargetExam(selectedExam)
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                authRepo = authRepo,
                onAuthSuccess = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MainShell.route) {
            MainShellScreen(
                mainViewModel = mainViewModel,
                newsViewModel = newsViewModel,
                performanceViewModel = performanceViewModel,
                knowledgeMapViewModel = knowledgeMapViewModel,
                pyqViewModel = pyqViewModel,
                onNavigateToArticle = { id ->
                    navController.navigate(Screen.NewsDetail.createRoute(id))
                },
                onNavigateToAnalysis = { id ->
                    navController.navigate(Screen.AiAnalysis.createRoute(id))
                },
                onNavigateToKnowledgeMapDetail = { topic, _ ->
                    navController.navigate(Screen.KnowledgeMapViewer.createRoute(topic))
                },
                onNavigateToQuizConfig = { content, title ->
                    quizViewModel.startQuizFromArticle(
                        articleText = content,
                        topic = title,
                        targetExam = "UPSC CSE",
                        count = 10,
                        difficulty = "Medium"
                    )
                    navController.navigate(Screen.ActiveQuiz.route)
                },
                onNavigateToRevision = { topic ->
                    revisionViewModel.startRevision(topic, listOf("Core concepts"), 44.0)
                    navController.navigate(Screen.RevisionDetail.createRoute(topic))
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.MainShell.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            NewsDetailScreen(
                articleId = articleId,
                viewModel = newsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAnalysis = { id ->
                    navController.navigate(Screen.AiAnalysis.createRoute(id))
                },
                onNavigateToKnowledgeMap = { topic, _ ->
                    navController.navigate(Screen.KnowledgeMapViewer.createRoute(topic))
                },
                onNavigateToQuizConfig = { content, title ->
                    quizViewModel.startQuizFromArticle(
                        articleText = content,
                        topic = title,
                        targetExam = "UPSC CSE",
                        count = 10,
                        difficulty = "Medium"
                    )
                    navController.navigate(Screen.ActiveQuiz.route)
                }
            )
        }

        composable(
            route = Screen.AiAnalysis.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            AiAnalysisScreen(
                articleId = articleId,
                newsViewModel = newsViewModel,
                analysisViewModel = analysisViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.KnowledgeMapViewer.route,
            arguments = listOf(navArgument("topic") { type = NavType.StringType })
        ) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: "Topic"
            KnowledgeMapScreen(
                topic = topic,
                contextText = "Detailed context regarding $topic for Indian competitive exams.",
                viewModel = knowledgeMapViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ActiveQuiz.route) {
            ActiveQuizScreen(
                viewModel = quizViewModel,
                onQuizCompleted = {
                    navController.navigate(Screen.QuizResult.route) {
                        popUpTo(Screen.ActiveQuiz.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QuizResult.route) {
            QuizResultScreen(
                viewModel = quizViewModel,
                onNavigateToRevision = { topic ->
                    revisionViewModel.startRevision(topic, emptyList(), 44.0)
                    navController.navigate(Screen.RevisionDetail.createRoute(topic))
                },
                onBackToDashboard = {
                    quizViewModel.resetQuiz()
                    navController.popBackStack(Screen.MainShell.route, inclusive = false)
                }
            )
        }

        composable(
            route = Screen.RevisionDetail.route,
            arguments = listOf(navArgument("topic") { type = NavType.StringType })
        ) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: "Topic"
            RevisionScreen(
                topic = topic,
                viewModel = revisionViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                bookmarkRepo = bookmarkRepo,
                onBack = { navController.popBackStack() },
                onNavigateToArticle = { id ->
                    navController.navigate(Screen.NewsDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = mainViewModel,
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.MainShell.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
