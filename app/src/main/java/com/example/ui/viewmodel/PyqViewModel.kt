package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PyqQuestion
import com.example.data.repository.PerformanceRepository
import com.example.data.repository.PyqRepository
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PyqMode {
    PRACTICE, // Instant explanation upon selecting answer
    MOCK_TEST  // Timed exam with submission
}

class PyqViewModel(
    private val pyqRepo: PyqRepository,
    private val quizRepo: QuizRepository,
    private val performanceRepo: PerformanceRepository
) : ViewModel() {

    private val _selectedExam = MutableStateFlow("All")
    val selectedExam: StateFlow<String> = _selectedExam.asStateFlow()

    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _mode = MutableStateFlow(PyqMode.PRACTICE)
    val mode: StateFlow<PyqMode> = _mode.asStateFlow()

    private val _questions = MutableStateFlow<List<PyqQuestion>>(emptyList())
    val questions: StateFlow<List<PyqQuestion>> = _questions.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<String, Int>> = _userAnswers.asStateFlow()

    private val _revealedExplanations = MutableStateFlow<Set<String>>(emptySet())
    val revealedExplanations: StateFlow<Set<String>> = _revealedExplanations.asStateFlow()

    val availableExams = listOf("All", "UPSC", "SSC CGL", "Banking", "Railway", "State PSC")
    val availableSubjects = listOf("All", "Economy", "Polity", "Science and Technology", "International Relations", "Banking Awareness", "National Affairs")

    init {
        loadPyqs()
    }

    fun setExam(exam: String) {
        _selectedExam.value = exam
        loadPyqs()
    }

    fun setSubject(subject: String) {
        _selectedSubject.value = subject
        loadPyqs()
    }

    fun setMode(mode: PyqMode) {
        _mode.value = mode
        _userAnswers.value = emptyMap()
        _revealedExplanations.value = emptySet()
    }

    fun selectAnswer(questionId: String, optionIndex: Int, isCorrect: Boolean) {
        val updated = _userAnswers.value.toMutableMap()
        updated[questionId] = optionIndex
        _userAnswers.value = updated

        if (_mode.value == PyqMode.PRACTICE) {
            val revealed = _revealedExplanations.value.toMutableSet()
            revealed.add(questionId)
            _revealedExplanations.value = revealed

            // Record single practice answer in performance
            val q = _questions.value.find { it.id == questionId }
            if (q != null) {
                viewModelScope.launch {
                    performanceRepo.updatePerformanceForAttempt(
                        topic = q.subject,
                        attempted = 1,
                        correct = if (isCorrect) 1 else 0,
                        weakConceptsToAdd = if (!isCorrect) listOf(q.topic) else emptyList()
                    )
                }
            }
        }
    }

    private fun loadPyqs() {
        viewModelScope.launch {
            val list = pyqRepo.getPyqs(
                examination = _selectedExam.value,
                subject = _selectedSubject.value
            )
            _questions.value = list
        }
    }
}
