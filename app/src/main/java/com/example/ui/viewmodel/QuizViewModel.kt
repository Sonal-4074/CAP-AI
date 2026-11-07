package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ExamQuestion
import com.example.data.model.QuizAttempt
import com.example.data.remote.GeminiAiService
import com.example.data.repository.PerformanceRepository
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuizSessionState {
    data object Idle : QuizSessionState
    data object Generating : QuizSessionState
    data class Active(
        val questions: List<ExamQuestion>,
        val currentIndex: Int = 0,
        val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex to selectedOptionIndex
        val markedForReview: Set<Int> = emptySet(),
        val remainingSeconds: Int = 600,
        val totalSeconds: Int = 600
    ) : QuizSessionState
    data class Completed(
        val attempt: QuizAttempt,
        val questions: List<ExamQuestion>,
        val userAnswers: Map<Int, Int>
    ) : QuizSessionState
    data class Error(val message: String) : QuizSessionState
}

class QuizViewModel(
    private val aiService: GeminiAiService,
    private val quizRepo: QuizRepository,
    private val performanceRepo: PerformanceRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<QuizSessionState>(QuizSessionState.Idle)
    val sessionState: StateFlow<QuizSessionState> = _sessionState.asStateFlow()

    private var timerJob: Job? = null

    fun startQuizFromArticle(
        articleText: String,
        topic: String,
        targetExam: String,
        count: Int,
        difficulty: String
    ) {
        viewModelScope.launch {
            _sessionState.value = QuizSessionState.Generating
            val result = aiService.generateQuestions(
                articleText = articleText,
                targetExam = targetExam,
                count = count,
                difficulty = difficulty
            )

            if (result.isSuccess) {
                val questions = result.getOrThrow()
                val totalTime = count * 60 // 1 min per question
                _sessionState.value = QuizSessionState.Active(
                    questions = questions,
                    remainingSeconds = totalTime,
                    totalSeconds = totalTime
                )
                startTimer()
            } else {
                _sessionState.value = QuizSessionState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to generate exam questions."
                )
            }
        }
    }

    fun startCustomQuizWithQuestions(questions: List<ExamQuestion>, topic: String) {
        val totalTime = questions.size * 60
        _sessionState.value = QuizSessionState.Active(
            questions = questions,
            remainingSeconds = totalTime,
            totalSeconds = totalTime
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _sessionState.value
                if (current is QuizSessionState.Active) {
                    if (current.remainingSeconds <= 1) {
                        submitQuiz()
                        break
                    } else {
                        _sessionState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                    }
                } else {
                    break
                }
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val current = _sessionState.value
        if (current is QuizSessionState.Active) {
            val updated = current.userAnswers.toMutableMap()
            updated[current.currentIndex] = optionIndex
            _sessionState.value = current.copy(userAnswers = updated)
        }
    }

    fun clearAnswer() {
        val current = _sessionState.value
        if (current is QuizSessionState.Active) {
            val updated = current.userAnswers.toMutableMap()
            updated.remove(current.currentIndex)
            _sessionState.value = current.copy(userAnswers = updated)
        }
    }

    fun toggleMarkForReview() {
        val current = _sessionState.value
        if (current is QuizSessionState.Active) {
            val marked = current.markedForReview.toMutableSet()
            if (marked.contains(current.currentIndex)) {
                marked.remove(current.currentIndex)
            } else {
                marked.add(current.currentIndex)
            }
            _sessionState.value = current.copy(markedForReview = marked)
        }
    }

    fun navigateToQuestion(index: Int) {
        val current = _sessionState.value
        if (current is QuizSessionState.Active && index in current.questions.indices) {
            _sessionState.value = current.copy(currentIndex = index)
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val current = _sessionState.value
        if (current !is QuizSessionState.Active) return

        viewModelScope.launch {
            val questions = current.questions
            val userAnswers = current.userAnswers
            val totalQuestions = questions.size
            val attempted = userAnswers.size
            var correct = 0
            val weakConceptsFound = mutableListOf<String>()

            for (i in questions.indices) {
                val q = questions[i]
                val userChoice = userAnswers[i]
                if (userChoice != null) {
                    if (userChoice == q.correctIndex) {
                        correct++
                    } else {
                        if (q.relatedConcept.isNotBlank()) {
                            weakConceptsFound.add(q.relatedConcept)
                        }
                    }
                }
            }

            val timeTaken = (current.totalSeconds - current.remainingSeconds).toLong()
            val topic = questions.firstOrNull()?.category ?: "General Awareness"
            val exam = questions.firstOrNull()?.examType ?: "UPSC"

            val attempt = quizRepo.recordQuizAttempt(
                quizType = "AI_QUIZ",
                topic = topic,
                targetExamination = exam,
                totalQuestions = totalQuestions,
                attemptedQuestions = attempted,
                correctAnswers = correct,
                timeTakenSeconds = timeTaken
            )

            // Update user performance and weakness detection
            performanceRepo.updatePerformanceForAttempt(
                topic = topic,
                attempted = attempted,
                correct = correct,
                weakConceptsToAdd = weakConceptsFound
            )

            _sessionState.value = QuizSessionState.Completed(
                attempt = attempt,
                questions = questions,
                userAnswers = userAnswers
            )
        }
    }

    fun resetQuiz() {
        timerJob?.cancel()
        _sessionState.value = QuizSessionState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
