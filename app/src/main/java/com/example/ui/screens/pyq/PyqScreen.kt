package com.example.ui.screens.pyq

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PyqQuestion
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.PyqMode
import com.example.ui.viewmodel.PyqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqScreen(
    viewModel: PyqViewModel
) {
    val questions by viewModel.questions.collectAsState()
    val selectedExam by viewModel.selectedExam.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val revealedExplanations by viewModel.revealedExplanations.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pyq_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Mode Selector Tab (Practice vs Mock)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                TabRow(
                    selectedTabIndex = if (mode == PyqMode.PRACTICE) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = mode == PyqMode.PRACTICE,
                        onClick = { viewModel.setMode(PyqMode.PRACTICE) },
                        text = { Text("Practice Mode (Instant Feedback)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = mode == PyqMode.MOCK_TEST,
                        onClick = { viewModel.setMode(PyqMode.MOCK_TEST) },
                        text = { Text("Mock Mode (Exam Conditions)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Exam & Subject Filters
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                // Exam Chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Examination:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.availableExams) { exam ->
                        val isSelected = selectedExam == exam
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setExam(exam) },
                            label = { Text(exam, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Subject:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.availableSubjects) { subject ->
                        val isSelected = selectedSubject == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSubject(subject) },
                            label = { Text(subject, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Count Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${questions.size} Verified Previous Year Questions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // PYQ Items List
        items(questions, key = { it.id }) { q ->
            val userChoice = userAnswers[q.id]
            val isRevealed = revealedExplanations.contains(q.id)

            PyqQuestionCard(
                question = q,
                userChoice = userChoice,
                isRevealed = isRevealed,
                onSelectOption = { optionIndex ->
                    val isCorrect = optionIndex == q.correctIndex
                    viewModel.selectAnswer(q.id, optionIndex, isCorrect)
                }
            )
        }
    }
}

@Composable
fun PyqQuestionCard(
    question: PyqQuestion,
    userChoice: Int?,
    isRevealed: Boolean,
    onSelectOption: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Exam reference header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoyalBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${question.examination} • ${question.year}",
                        color = RoyalBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = question.subject,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options
            question.options.forEachIndexed { optIndex, optionText ->
                val isSelected = userChoice == optIndex
                val isCorrectAnswer = optIndex == question.correctIndex

                val backgroundColor = when {
                    isRevealed && isCorrectAnswer -> Color(0xFFECFDF5)
                    isRevealed && isSelected && !isCorrectAnswer -> Color(0xFFFEF2F2)
                    isSelected -> RoyalBlue.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor)
                        .clickable { onSelectOption(optIndex) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isRevealed && isCorrectAnswer -> Color(0xFF10B981)
                            isRevealed && isSelected && !isCorrectAnswer -> Color(0xFFEF4444)
                            isSelected -> RoyalBlue
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ('A'.code + optIndex).toChar().toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected || (isRevealed && isCorrectAnswer)) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = optionText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isRevealed && isCorrectAnswer) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Correct",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isRevealed && isSelected && !isCorrectAnswer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Incorrect",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Explanation box when revealed
            if (isRevealed) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Official Explanation:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = RoyalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reference: ${question.sourceReference}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
