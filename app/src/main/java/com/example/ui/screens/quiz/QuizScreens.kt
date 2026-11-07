package com.example.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamQuestion
import com.example.data.model.QuizAttempt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MasteryGood
import com.example.ui.theme.MasteryNeedsWork
import com.example.ui.theme.MasteryStrong
import com.example.ui.theme.MasteryWeak
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftPurple
import com.example.ui.viewmodel.QuizSessionState
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    articleContent: String,
    articleTitle: String,
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onStartTest: () -> Unit
) {
    var selectedCount by remember { mutableIntStateOf(10) }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedExam by remember { mutableStateOf("UPSC CSE") }

    val counts = listOf(5, 10, 20)
    val difficulties = listOf("Easy", "Medium", "Hard")
    val exams = listOf("UPSC CSE", "SSC CGL", "Banking (IBPS/SBI)", "Railway")

    val sessionState by viewModel.sessionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Question Generation", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Source Topic",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = articleTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Target Examination Selection
                Text("Select Target Examination:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exams) { exam ->
                        FilterChip(
                            selected = selectedExam == exam,
                            onClick = { selectedExam = exam },
                            label = { Text(exam) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Number of Questions
                Text("Number of Questions:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    counts.forEach { count ->
                        FilterChip(
                            selected = selectedCount == count,
                            onClick = { selectedCount = count },
                            label = { Text("$count Questions") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Difficulty Level
                Text("Difficulty Level:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    difficulties.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Launch Button
            Column {
                if (sessionState is QuizSessionState.Generating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RoyalBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gemini is crafting exam questions...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        viewModel.startQuizFromArticle(
                            articleText = articleContent,
                            topic = articleTitle,
                            targetExam = selectedExam,
                            count = selectedCount,
                            difficulty = selectedDifficulty
                        )
                        onStartTest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_quiz_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    enabled = sessionState !is QuizSessionState.Generating
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Start Quiz", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    viewModel: QuizViewModel,
    onQuizCompleted: () -> Unit
) {
    val sessionState by viewModel.sessionState.collectAsState()
    var showSubmitDialog by remember { mutableStateOf(false) }

    if (sessionState is QuizSessionState.Completed) {
        onQuizCompleted()
        return
    }

    if (sessionState !is QuizSessionState.Active) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalBlue)
        }
        return
    }

    val state = sessionState as QuizSessionState.Active
    val questions = state.questions
    val currentQuestion = questions[state.currentIndex]
    val selectedOption = state.userAnswers[state.currentIndex]
    val isMarkedForReview = state.markedForReview.contains(state.currentIndex)

    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Q ${state.currentIndex + 1} of ${questions.size}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Timer Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (state.remainingSeconds < 60) Color(0xFFFEE2E2) else RoyalBlue.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (state.remainingSeconds < 60) MasteryWeak else RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formattedTime,
                                    color = if (state.remainingSeconds < 60) MasteryWeak else RoyalBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { showSubmitDialog = true }) {
                        Text("Submit", fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { (state.currentIndex + 1).toFloat() / questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RoyalBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = currentQuestion.category,
                                    color = RoyalBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (isMarkedForReview) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Marked for Review", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQuestion.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options List
                currentQuestion.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedOption == optIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { viewModel.selectAnswer(optIndex) }
                            .testTag("option_${optIndex}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RoyalBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
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
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ('A'.code + optIndex).toChar().toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optionText,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // Bottom Navigation & Actions
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.toggleMarkForReview() }) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (isMarkedForReview) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isMarkedForReview) "Unmark" else "Mark for Review")
                    }

                    if (selectedOption != null) {
                        TextButton(onClick = { viewModel.clearAnswer() }) {
                            Text("Clear Response", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateToQuestion(state.currentIndex - 1) },
                        enabled = state.currentIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (state.currentIndex < questions.size - 1) {
                                viewModel.navigateToQuestion(state.currentIndex + 1)
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("next_or_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text(if (state.currentIndex == questions.size - 1) "Review & Submit" else "Next")
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        val attemptedCount = state.userAnswers.size
        val unattemptedCount = questions.size - attemptedCount
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Assessment?") },
            text = {
                Text(
                    "You have attempted $attemptedCount out of ${questions.size} questions ($unattemptedCount unattempted). Are you ready to submit and calculate your performance?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitQuiz()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Submit Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Quiz")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    viewModel: QuizViewModel,
    onNavigateToRevision: (String) -> Unit,
    onBackToDashboard: () -> Unit
) {
    val sessionState by viewModel.sessionState.collectAsState()

    if (sessionState !is QuizSessionState.Completed) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = onBackToDashboard) { Text("Back to Dashboard") }
        }
        return
    }

    val completedState = sessionState as QuizSessionState.Completed
    val attempt = completedState.attempt
    val questions = completedState.questions
    val userAnswers = completedState.userAnswers

    val accuracyColor = when {
        attempt.accuracy < 50.0 -> MasteryWeak
        attempt.accuracy < 70.0 -> MasteryNeedsWork
        attempt.accuracy < 85.0 -> RoyalBlue
        else -> MasteryStrong
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assessment Report", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Done")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("quiz_result_screen"),
            contentPadding = PaddingValues(20.dp)
        ) {
            // Score Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDeep)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = attempt.topic,
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Circular Accuracy Gauge
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                            CircularProgressIndicator(
                                progress = { (attempt.accuracy / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.size(110.dp),
                                color = accuracyColor,
                                strokeWidth = 8.dp,
                                trackColor = Color(0xFF1E2F5B)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${attempt.accuracy.toInt()}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Accuracy",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ResultStat(title = "Score", value = String.format("%.2f", attempt.score))
                            ResultStat(title = "Correct", value = "${attempt.correctAnswers}")
                            ResultStat(title = "Incorrect", value = "${attempt.incorrectAnswers}")
                            ResultStat(title = "Unattempted", value = "${attempt.unattemptedQuestions}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Revise Weak Concepts CTA
            if (attempt.accuracy < 70.0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weakness Detected",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9F1239)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Accuracy is below 70%. Personalized revision notes and re-tests ready.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF881337)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onNavigateToRevision(attempt.topic) },
                                colors = ButtonDefaults.buttonColors(containerColor = MasteryWeak),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Revise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Question Review List Header
            item {
                Text(
                    text = "Question-Wise Answer Key & Explanations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(questions) { index, q ->
                val userChoice = userAnswers[index]
                val isCorrect = userChoice == q.correctIndex
                val isAnswered = userChoice != null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalBlue
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                                    isCorrect -> Color(0xFFECFDF5)
                                    else -> Color(0xFFFEF2F2)
                                }
                            ) {
                                Text(
                                    text = when {
                                        !isAnswered -> "Unattempted"
                                        isCorrect -> "Correct (+1.00)"
                                        else -> "Incorrect (-0.25)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        !isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                                        isCorrect -> Color(0xFF047857)
                                        else -> Color(0xFFB91C1C)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = q.question,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Correct option & user option
                        q.options.forEachIndexed { optIndex, text ->
                            val isUserSelected = userChoice == optIndex
                            val isRightAnswer = q.correctIndex == optIndex

                            val bgColor = when {
                                isRightAnswer -> Color(0xFFECFDF5)
                                isUserSelected -> Color(0xFFFEF2F2)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A'.code + optIndex).toChar()}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = when {
                                        isRightAnswer -> Color(0xFF047857)
                                        isUserSelected -> Color(0xFFB91C1C)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pedagogical Explanation Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Explanation:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = RoyalBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = q.explanation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ResultStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
