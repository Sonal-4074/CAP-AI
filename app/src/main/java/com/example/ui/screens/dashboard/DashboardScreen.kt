package com.example.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Article
import com.example.data.model.TopicPerformance
import com.example.data.model.UserProfile
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MasteryNeedsWork
import com.example.ui.theme.MasteryStrong
import com.example.ui.theme.MasteryWeak
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftPurple
import com.example.ui.viewmodel.NewsViewModel
import com.example.ui.viewmodel.PerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    newsViewModel: NewsViewModel,
    performanceViewModel: PerformanceViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToAnalysis: (String) -> Unit,
    onNavigateToKnowledgeMap: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    onNavigateToRevision: (String) -> Unit,
    onNavigateToBookmarks: () -> Unit
) {
    val articles by newsViewModel.filteredArticles.collectAsState()
    val perfOverview by performanceViewModel.overview.collectAsState()
    val weakTopics = perfOverview.weakTopics

    val userName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "Aspirant"
    val targetExam = userProfile?.targetExamination ?: "UPSC CSE"
    val streak = userProfile?.studyStreak ?: 4L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyDeep, NavyDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon_1794034553667),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hi, $userName 👋",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target: $targetExam",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Streak Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF23366E)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🔥", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$streak d",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = onNavigateToBookmarks) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Bookmarks",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Daily Preparation Summary Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetricCard(
                            title = "Attempted",
                            value = "${perfOverview.totalAttempted}",
                            subtext = "Questions",
                            color = RoyalBlue,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Accuracy",
                            value = "${perfOverview.overallAccuracy.toInt()}%",
                            subtext = if (perfOverview.overallAccuracy >= 70) "High" else "Improving",
                            color = if (perfOverview.overallAccuracy >= 70) MasteryStrong else MasteryNeedsWork,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Articles",
                            value = "${articles.size}",
                            subtext = "Today's Feed",
                            color = SoftPurple,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Weak Topics",
                            value = "${weakTopics.size}",
                            subtext = "Needs Review",
                            color = if (weakTopics.isNotEmpty()) MasteryWeak else MasteryStrong,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Hero Banner Art
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_study_hero_1794034570419),
                    contentDescription = "AI Study Assistant",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )

                // Overlay gradient with action CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    NavyDeep.copy(alpha = 0.92f),
                                    NavyDeep.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.width(220.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ADAPTIVE AI ENGINE",
                                color = ElectricCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Current Affairs to Knowledge Maps",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Connect concepts, generate MCQs & master syllabus",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Weakness Alert Card (Prominent Call to Action)
        if (weakTopics.isNotEmpty()) {
            item {
                val primaryWeak = weakTopics.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("weak_topic_alert"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF1F2)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MasteryWeak.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MasteryWeak,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Weak Topic Detected",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9F1239)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MasteryWeak
                                ) {
                                    Text(
                                        text = "${primaryWeak.accuracy.toInt()}% Accuracy",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${primaryWeak.topic} (${primaryWeak.weakConcepts.take(2).joinToString(", ")})",
                                fontSize = 12.sp,
                                color = Color(0xFF881337),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onNavigateToRevision(primaryWeak.topic) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MasteryWeak
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("revise_button")
                        ) {
                            Text("Revise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // AI Personalized Recommendations
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Personalized Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val recommendations = listOf(
                    RecommendationItem(
                        title = "Practice Questions: RBI Monetary Policy",
                        category = "Indian Economy",
                        actionLabel = "Generate 10 MCQs",
                        action = {
                            val art = articles.firstOrNull { it.category == "Economy" } ?: articles.firstOrNull()
                            if (art != null) onNavigateToQuiz(art.content, "Indian Economy")
                        }
                    ),
                    RecommendationItem(
                        title = "Explore Mind Map: ISRO Chandrayaan-4",
                        category = "Science and Technology",
                        actionLabel = "View Mind Map",
                        action = {
                            val art = articles.firstOrNull { it.category == "Science and Technology" } ?: articles.firstOrNull()
                            if (art != null) onNavigateToKnowledgeMap("Chandrayaan-4", art.content)
                        }
                    ),
                    RecommendationItem(
                        title = "Revise: COP29 & Climate Finance Targets",
                        category = "Environment",
                        actionLabel = "Quick Revision",
                        action = {
                            onNavigateToRevision("Environment & Ecology")
                        }
                    )
                )

                recommendations.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { item.action() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.actionLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Current Affairs Section
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Current Affairs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(articles) { article ->
                        ArticleDashboardCard(
                            article = article,
                            onReadMore = { onNavigateToArticle(article.id) },
                            onAiAnalysis = { onNavigateToAnalysis(article.id) },
                            onMindMap = { onNavigateToKnowledgeMap(article.title, article.content) }
                        )
                    }
                }
            }
        }

        // Topic Mastery Progress
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subject Mastery Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                perfOverview.allTopics.forEach { topicPerf ->
                    TopicMasteryBar(topicPerf = topicPerf)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131F42)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ArticleDashboardCard(
    article: Article,
    onReadMore: () -> Unit,
    onAiAnalysis: () -> Unit,
    onMindMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onReadMore() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoyalBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = article.category,
                        color = RoyalBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = article.publishedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = article.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAiAnalysis() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Analysis",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMindMap() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mind Map",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicMasteryBar(topicPerf: TopicPerformance) {
    val progress = (topicPerf.accuracy / 100.0).toFloat().coerceIn(0f, 1f)
    val statusColor = when (topicPerf.masteryLevel) {
        "Weak" -> MasteryWeak
        "Needs Improvement" -> MasteryNeedsWork
        "Good" -> RoyalBlue
        else -> MasteryStrong
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topicPerf.topic,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${topicPerf.masteryLevel} (${topicPerf.accuracy.toInt()}%)",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (topicPerf.weakConcepts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Weak Concepts: ${topicPerf.weakConcepts.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class RecommendationItem(
    val title: String,
    val category: String,
    val actionLabel: String,
    val action: () -> Unit
)
