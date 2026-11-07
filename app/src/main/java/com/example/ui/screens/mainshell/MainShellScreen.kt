package com.example.ui.screens.mainshell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomTab
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.knowledgemap.KnowledgeMapScreen
import com.example.ui.screens.news.NewsListScreen
import com.example.ui.screens.performance.PerformanceScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.pyq.PyqScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.KnowledgeMapViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NewsViewModel
import com.example.ui.viewmodel.PerformanceViewModel
import com.example.ui.viewmodel.PyqViewModel

@Composable
fun MainShellScreen(
    mainViewModel: MainViewModel,
    newsViewModel: NewsViewModel,
    performanceViewModel: PerformanceViewModel,
    knowledgeMapViewModel: KnowledgeMapViewModel,
    pyqViewModel: PyqViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToAnalysis: (String) -> Unit,
    onNavigateToKnowledgeMapDetail: (String, String) -> Unit,
    onNavigateToQuizConfig: (String, String) -> Unit,
    onNavigateToRevision: (String) -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(BottomTab.DASHBOARD) }
    val userProfile by mainViewModel.userProfile.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == BottomTab.DASHBOARD,
                    onClick = { currentTab = BottomTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue,
                        indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentTab == BottomTab.NEWS,
                    onClick = { currentTab = BottomTab.NEWS },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Current Affairs") },
                    label = { Text("News", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue,
                        indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_news")
                )

                NavigationBarItem(
                    selected = currentTab == BottomTab.KNOWLEDGE_MAP,
                    onClick = { currentTab = BottomTab.KNOWLEDGE_MAP },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Mind Maps") },
                    label = { Text("Mind Maps", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue,
                        indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_maps")
                )

                NavigationBarItem(
                    selected = currentTab == BottomTab.PYQ,
                    onClick = { currentTab = BottomTab.PYQ },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "PYQs") },
                    label = { Text("PYQs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue,
                        indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_pyq")
                )

                NavigationBarItem(
                    selected = currentTab == BottomTab.ANALYTICS,
                    onClick = { currentTab = BottomTab.ANALYTICS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue,
                        indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomTab.DASHBOARD -> DashboardScreen(
                    userProfile = userProfile,
                    newsViewModel = newsViewModel,
                    performanceViewModel = performanceViewModel,
                    onNavigateToArticle = onNavigateToArticle,
                    onNavigateToAnalysis = onNavigateToAnalysis,
                    onNavigateToKnowledgeMap = onNavigateToKnowledgeMapDetail,
                    onNavigateToQuiz = onNavigateToQuizConfig,
                    onNavigateToRevision = onNavigateToRevision,
                    onNavigateToBookmarks = onNavigateToBookmarks
                )

                BottomTab.NEWS -> NewsListScreen(
                    viewModel = newsViewModel,
                    onNavigateToArticle = onNavigateToArticle,
                    onNavigateToAnalysis = onNavigateToAnalysis,
                    onNavigateToKnowledgeMap = onNavigateToKnowledgeMapDetail
                )

                BottomTab.KNOWLEDGE_MAP -> KnowledgeMapScreen(
                    topic = "RBI Monetary Policy & Inflation Framework",
                    contextText = "The Reserve Bank of India MPC maintained the repo rate at 6.50% with standing deposit facility at 6.25% and marginal standing facility at 6.75% focusing on anchoring 4% inflation.",
                    viewModel = knowledgeMapViewModel,
                    onBack = { currentTab = BottomTab.DASHBOARD }
                )

                BottomTab.PYQ -> PyqScreen(
                    viewModel = pyqViewModel
                )

                BottomTab.ANALYTICS -> PerformanceScreen(
                    viewModel = performanceViewModel,
                    onNavigateToRevision = onNavigateToRevision
                )
            }
        }
    }
}
