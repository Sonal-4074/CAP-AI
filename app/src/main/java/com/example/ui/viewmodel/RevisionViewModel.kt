package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ExamQuestion
import com.example.data.model.RevisionSession
import com.example.data.repository.RevisionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RevisionUiState {
    data object Idle : RevisionUiState
    data object Generating : RevisionUiState
    data class StudyPhase(val session: RevisionSession) : RevisionUiState
    data class ReTestPhase(
        val session: RevisionSession,
        val currentIndex: Int = 0,
        val userAnswers: Map<Int, Int> = emptyMap()
    ) : RevisionUiState
    data class Completed(
        val session: RevisionSession,
        val previousAccuracy: Double,
        val newAccuracy: Double,
        val improvement: Double,
        val masteryUpdated: Boolean
    ) : RevisionUiState
    data class Error(val message: String) : RevisionUiState
}

class RevisionViewModel(
    private val revisionRepo: RevisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevisionUiState>(RevisionUiState.Idle)
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    fun startRevision(topic: String, weakConcepts: List<String>, previousAccuracy: Double) {
        viewModelScope.launch {
            _uiState.value = RevisionUiState.Generating
            val result = revisionRepo.startRevisionSession(topic, weakConcepts, previousAccuracy)
            if (result.isSuccess) {
                _uiState.value = RevisionUiState.StudyPhase(result.getOrThrow())
            } else {
                _uiState.value = RevisionUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to generate AI revision module."
                )
            }
        }
    }

    fun startReTest() {
        val current = _uiState.value
        if (current is RevisionUiState.StudyPhase) {
            _uiState.value = RevisionUiState.ReTestPhase(
                session = current.session,
                currentIndex = 0,
                userAnswers = emptyMap()
            )
        }
    }

    fun selectReTestAnswer(optionIndex: Int) {
        val current = _uiState.value
        if (current is RevisionUiState.ReTestPhase) {
            val updated = current.userAnswers.toMutableMap()
            updated[current.currentIndex] = optionIndex
            _uiState.value = current.copy(userAnswers = updated)
        }
    }

    fun nextQuestion() {
        val current = _uiState.value
        if (current is RevisionUiState.ReTestPhase && current.currentIndex < current.session.questions.size - 1) {
            _uiState.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun prevQuestion() {
        val current = _uiState.value
        if (current is RevisionUiState.ReTestPhase && current.currentIndex > 0) {
            _uiState.value = current.copy(currentIndex = current.currentIndex - 1)
        }
    }

    fun submitReTest() {
        val current = _uiState.value
        if (current !is RevisionUiState.ReTestPhase) return

        viewModelScope.launch {
            val questions = current.session.questions
            val answers = current.userAnswers
            var correct = 0
            for (i in questions.indices) {
                if (answers[i] == questions[i].correctIndex) {
                    correct++
                }
            }

            val newAccuracy = revisionRepo.completeRevisionReTest(
                topic = current.session.topic,
                totalQuestions = questions.size,
                correctAnswers = correct,
                previousAccuracy = current.session.previousAccuracy
            )

            val improvement = newAccuracy - current.session.previousAccuracy
            _uiState.value = RevisionUiState.Completed(
                session = current.session,
                previousAccuracy = current.session.previousAccuracy,
                newAccuracy = newAccuracy,
                improvement = improvement,
                masteryUpdated = true
            )
        }
    }

    fun reset() {
        _uiState.value = RevisionUiState.Idle
    }
}
