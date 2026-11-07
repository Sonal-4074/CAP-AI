package com.example.ui.screens.analysis

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiAnalysis
import com.example.data.model.Article
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftPurple
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.AnalysisViewModel
import com.example.ui.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    articleId: String,
    newsViewModel: NewsViewModel,
    analysisViewModel: AnalysisViewModel,
    onBack: () -> Unit
) {
    val articles by newsViewModel.filteredArticles.collectAsState()
    val article = articles.find { it.id == articleId }
    val uiState by analysisViewModel.uiState.collectAsState()

    LaunchedEffect(articleId) {
        if (article != null && uiState is AnalysisUiState.Idle) {
            analysisViewModel.generateAnalysis(article)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI News Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("ai_analysis_screen")
        ) {
            when (val state = uiState) {
                is AnalysisUiState.Loading, is AnalysisUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = RoyalBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing with Gemini AI...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extracting exam relevance, key facts, definitions, and probable questions.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is AnalysisUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Analysis Unavailable",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (article != null) analysisViewModel.generateAnalysis(article)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Retry Analysis")
                        }
                    }
                }

                is AnalysisUiState.Success -> {
                    val analysis = state.analysis
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        // Title header
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RoyalBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = article?.category ?: "Current Affairs",
                                    color = RoyalBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = article?.title ?: "News Article",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        // 7 Expandable Sections
                        item {
                            AnalysisExpandableCard(
                                title = "A. Simple Explanation",
                                icon = Icons.Default.Translate,
                                iconTint = RoyalBlue,
                                initiallyExpanded = true
                            ) {
                                Text(
                                    text = analysis.simpleExplanation,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "B. Important Facts (Prelims)",
                                icon = Icons.Default.CalendarToday,
                                iconTint = Color(0xFF0D9488),
                                initiallyExpanded = true
                            ) {
                                analysis.keyFacts.forEach { fact ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D9488),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = fact,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "C. Exam & Syllabus Relevance",
                                icon = Icons.Default.School,
                                iconTint = IndigoPrimary,
                                initiallyExpanded = true
                            ) {
                                Text(
                                    text = analysis.examRelevance,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "D. Important Keywords & Definitions",
                                icon = Icons.Default.Key,
                                iconTint = Color(0xFFD97706),
                                initiallyExpanded = false
                            ) {
                                analysis.keywords.forEach { kw ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = kw.term,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFB45309)
                                        )
                                        Text(
                                            text = kw.definition,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "E. Background Context",
                                icon = Icons.Default.History,
                                iconTint = Color(0xFF475569),
                                initiallyExpanded = false
                            ) {
                                Text(
                                    text = analysis.backgroundInfo,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "F. Possible Examination Questions",
                                icon = Icons.Default.QuestionAnswer,
                                iconTint = Color(0xFF7C3AED),
                                initiallyExpanded = false
                            ) {
                                analysis.possibleExamQuestions.forEachIndexed { i, q ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "Q${i + 1}.",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7C3AED),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = q,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            AnalysisExpandableCard(
                                title = "G. Quick Revision Summary",
                                icon = Icons.Default.Speed,
                                iconTint = Color(0xFF10B981),
                                initiallyExpanded = true
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFECFDF5),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = analysis.quickRevision,
                                        fontSize = 13.sp,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisExpandableCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}
