package com.example.ui.screens.revision

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MasteryNeedsWork
import com.example.ui.theme.MasteryStrong
import com.example.ui.theme.MasteryWeak
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.RevisionUiState
import com.example.ui.viewmodel.RevisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    topic: String,
    viewModel: RevisionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(topic) {
        if (uiState is RevisionUiState.Idle) {
            viewModel.startRevision(
                topic = topic,
                weakConcepts = listOf("Core principles", "Common exam traps"),
                previousAccuracy = 44.0
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Personalized Revision", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("revision_screen")
        ) {
            when (val state = uiState) {
                is RevisionUiState.Generating, is RevisionUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = RoyalBlue, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "AI is generating targeted revision for $topic...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Extracting conceptual explanations, high-yield bullet facts, and 10 targeted re-test questions.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is RevisionUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Failed to Generate Revision", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.startRevision(topic, emptyList(), 44.0) }) {
                            Text("Retry")
                        }
                    }
                }

                // Phase 1: High-yield notes & explanation
                is RevisionUiState.StudyPhase -> {
                    val session = state.session
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = NavyDeep)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = session.topic, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(shape = RoundedCornerShape(8.dp), color = MasteryWeak) {
                                            Text(
                                                text = "Initial: ${session.previousAccuracy.toInt()}%",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Targeted Mastery Revision",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Conceptual explanation
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Core Concept Clarification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = session.explanation, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // High-yield facts
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("High-Yield Facts to Remember", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    session.keyFacts.forEach { fact ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                            Text(text = "•", fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(end = 8.dp))
                                            Text(text = fact, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Start Re-test CTA
                        item {
                            Button(
                                onClick = { viewModel.startReTest() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("start_retest_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Attempt Targeted Re-Test (${session.questions.size} Questions)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

                // Phase 2: Active Re-Test
                is RevisionUiState.ReTestPhase -> {
                    val questions = state.session.questions
                    val currentQ = questions[state.currentIndex]
                    val selectedOpt = state.userAnswers[state.currentIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Re-Test: Q ${state.currentIndex + 1} of ${questions.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                TextButton(onClick = { viewModel.submitReTest() }) {
                                    Text("Finish", fontWeight = FontWeight.Bold, color = RoyalBlue)
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (state.currentIndex + 1).toFloat() / questions.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RoyalBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = currentQ.question,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            currentQ.options.forEachIndexed { optIndex, optText ->
                                val isSelected = selectedOpt == optIndex
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.selectReTestAnswer(optIndex) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) RoyalBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, RoyalBlue) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = ('A'.code + optIndex).toChar().toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = optText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.prevQuestion() },
                                enabled = state.currentIndex > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Previous")
                            }

                            Button(
                                onClick = {
                                    if (state.currentIndex < questions.size - 1) {
                                        viewModel.nextQuestion()
                                    } else {
                                        viewModel.submitReTest()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text(if (state.currentIndex == questions.size - 1) "Submit Re-Test" else "Next")
                            }
                        }
                    }
                }

                // Phase 3: Completed & Comparison
                is RevisionUiState.Completed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = MasteryStrong, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Revision Completed!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Topic: ${state.session.topic}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Comparison Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Previous Accuracy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${state.previousAccuracy.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MasteryWeak)
                                    }

                                    Text("➔", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("New Accuracy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${state.newAccuracy.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MasteryStrong)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFECFDF5)) {
                                    Text(
                                        text = "Mastery Level Updated • ${if (state.improvement >= 0) "+${state.improvement.toInt()}%" else "${state.improvement.toInt()}%"} Improvement",
                                        color = Color(0xFF065F46),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Back to Analytics", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
