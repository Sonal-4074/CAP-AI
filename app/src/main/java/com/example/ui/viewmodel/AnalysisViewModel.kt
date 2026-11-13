package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiAnalysis
import com.example.data.model.Article
import com.example.data.remote.GroqAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Loading : AnalysisUiState
    data class Success(val analysis: AiAnalysis) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

class AnalysisViewModel(
    private val aiService: GroqAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun generateAnalysis(article: Article) {
        viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading
            val result = aiService.analyzeNews(article)
            if (result.isSuccess) {
                _uiState.value = AnalysisUiState.Success(result.getOrThrow())
            } else {
                _uiState.value = AnalysisUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to generate AI analysis. Please verify your connection."
                )
            }
        }
    }
}
