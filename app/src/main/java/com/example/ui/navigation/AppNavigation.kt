package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.remote.GroqAiService
import com.example.data.remote.api.ApiClient
import com.example.data.remote.api.TokenManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.PerformanceRepository
import com.example.data.repository.PyqRepository
import com.example.data.repository.QuizRepository
import com.example.data.repository.RevisionRepository
import com.example.ui.screens.analysis.AiAnalysisScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.EmailVerificationScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.ProfileSetupScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.ResetPasswordScreen
import com.example.ui.screens.auth.WelcomeScreen
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
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.KnowledgeMapViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NewsViewModel
import com.example.ui.viewmodel.PerformanceViewModel
import com.example.ui.viewmodel.PyqViewModel
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.RevisionViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Provisioned database ID from config
    val dbId = remember {
        val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else "ai-studio-android-capai-6a66acad-32ef-4aeb-a2ed-54123f94e883"
    }

    val firestore = remember {
        FirebaseFirestore.getInstance(dbId)
    }

    val database = remember { AppDatabase.getInstance(context) }
    val dao = remember { database.capDao() }

    val tokenManager = remember { TokenManager(context) }
    val apiService = remember { ApiClient.getService(context, tokenManager) }
    val aiService = remember { GroqAiService(apiService) }

    val authRepo = remember { AuthRepository(context, firestore, tokenManager) }
    val newsRepo = remember { NewsRepository(dao) }
    val bookmarkRepo = remember { BookmarkRepository(dao, firestore) }
    val quizRepo = remember { QuizRepository(dao, firestore) }
    val performanceRepo = remember { PerformanceRepository(dao, firestore) }
    val pyqRepo = remember { PyqRepository() }
    val revisionRepo = remember { RevisionRepository(dao, firestore ?: FirebaseFirestore.getInstance(), aiService, performanceRepo) }

    val authViewModel = remember { AuthViewModel(authRepo) }
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
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (tokenManager.hasToken()) {
                        navController.navigate(Screen.MainShell.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onCreateAccountClick = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // 3. Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = { email, debugOtp ->
                    navController.navigate(Screen.EmailVerification.route)
                }
            )
        }

        // 4. Email Verification Screen
        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                email = authViewModel.pendingEmail,
                debugOtp = authViewModel.pendingDebugOtp,
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onVerificationSuccess = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // 5. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToVerification = { email, debugOtp ->
                    navController.navigate(Screen.EmailVerification.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // 6. Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOtpSent = { email ->
                    navController.navigate(Screen.ResetPassword.route)
                }
            )
        }

        // 7. Reset Password Screen
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                email = authViewModel.pendingEmail,
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        // 8. Profile Setup Screen
        composable(Screen.ProfileSetup.route) {
            val userProfile = authRepo.userProfile.collectAsState().value
            ProfileSetupScreen(
                userProfile = userProfile,
                viewModel = authViewModel,
                onComplete = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen (Optional secondary route)
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = { selectedExam ->
                    mainViewModel.updateTargetExam(selectedExam)
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Compatibility Auth route
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

        // 9. Main Application Shell (Home Dashboard, Current Affairs, Mind Maps, PYQs, Analytics)
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
                    authViewModel.signOut {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.MainShell.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Detail Screens
        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
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
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
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
            val topic = backStackEntry.arguments?.getString("topic") ?: return@composable
            KnowledgeMapScreen(
                topic = topic,
                contextText = "",
                viewModel = knowledgeMapViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuizConfig.route) {
            QuizConfigScreen(
                articleContent = "",
                articleTitle = "Current Affairs Practice",
                viewModel = quizViewModel,
                onBack = { navController.popBackStack() },
                onStartTest = {
                    navController.navigate(Screen.ActiveQuiz.route)
                }
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
                    revisionViewModel.startRevision(topic, listOf("Missed quiz concepts"), 40.0)
                    navController.navigate(Screen.RevisionDetail.createRoute(topic))
                },
                onBackToDashboard = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.MainShell.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Screen.RevisionDetail.route,
            arguments = listOf(navArgument("topic") { type = NavType.StringType })
        ) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: return@composable
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
                    authViewModel.signOut {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.MainShell.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
