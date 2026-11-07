package com.example.ui.screens.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TopicPerformance
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MasteryGood
import com.example.ui.theme.MasteryNeedsWork
import com.example.ui.theme.MasteryStrong
import com.example.ui.theme.MasteryWeak
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftPurple
import com.example.ui.viewmodel.PerformanceViewModel

@Composable
fun PerformanceScreen(
    viewModel: PerformanceViewModel,
    onNavigateToRevision: (String) -> Unit
) {
    val overview by viewModel.overview.collectAsState()
    val allTopics by viewModel.allPerformance.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("performance_screen")
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Overview Summary Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Preparation Analytics",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real user accuracy & weakness tracking",
                                color = ElectricCyan,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoyalBlue.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "${overview.overallAccuracy.toInt()}% Overall",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AnalyticStatItem(title = "Attempted", value = "${overview.totalAttempted}")
                        AnalyticStatItem(title = "Correct", value = "${overview.totalCorrect}", valueColor = MasteryStrong)
                        AnalyticStatItem(title = "Incorrect", value = "${overview.totalIncorrect}", valueColor = MasteryWeak)
                        AnalyticStatItem(title = "Weak Topics", value = "${overview.weakTopicsCount}", valueColor = MasteryNeedsWork)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Weekly Activity Graphic Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weekly Activity & Consistency",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7-day activity bar chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val values = listOf(15, 22, 18, 30, 25, 28, 20)
                        val maxVal = values.maxOrNull() ?: 30

                        days.zip(values).forEach { (day, count) ->
                            val heightRatio = count.toFloat() / maxVal.toFloat()
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$count",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((65 * heightRatio).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (count >= 25) RoyalBlue else RoyalBlue.copy(alpha = 0.35f))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Topic-wise Performance with Mastery Thresholds
        item {
            Text(
                text = "Topic-Wise Mastery Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "<50% Weak • 50-69% Needs Improvement • 70-84% Good • 85%+ Strong",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(allTopics, key = { it.topic }) { topicPerf ->
            TopicPerformanceDetailedCard(
                topicPerf = topicPerf,
                onReviseClick = { onNavigateToRevision(topicPerf.topic) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TopicPerformanceDetailedCard(
    topicPerf: TopicPerformance,
    onReviseClick: () -> Unit
) {
    val progress = (topicPerf.accuracy / 100.0).toFloat().coerceIn(0f, 1f)
    val statusColor = when (topicPerf.masteryLevel) {
        "Weak" -> MasteryWeak
        "Needs Improvement" -> MasteryNeedsWork
        "Good" -> RoyalBlue
        else -> MasteryStrong
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topicPerf.topic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${topicPerf.attempted} attempted • ${topicPerf.correct} correct",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${topicPerf.masteryLevel} (${topicPerf.accuracy.toInt()}%)",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Weak concepts & repeated mistakes section
            if (topicPerf.weakConcepts.isNotEmpty() || topicPerf.repeatedMistakes > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (topicPerf.masteryLevel == "Weak") Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (topicPerf.weakConcepts.isNotEmpty()) {
                            Text(
                                text = "Weak Concepts: ${topicPerf.weakConcepts.joinToString(", ")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (topicPerf.masteryLevel == "Weak") Color(0xFF9F1239) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (topicPerf.repeatedMistakes > 0) {
                            Text(
                                text = "Repeated Mistakes: ${topicPerf.repeatedMistakes} detected",
                                fontSize = 11.sp,
                                color = MasteryWeak,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (topicPerf.accuracy < 70.0) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReviseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start AI Revision for ${topicPerf.topic}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnalyticStatItem(title: String, value: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
