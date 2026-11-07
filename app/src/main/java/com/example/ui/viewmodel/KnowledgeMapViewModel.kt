package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.KnowledgeMapData
import com.example.data.model.MapNode
import com.example.data.remote.GeminiAiService
import com.example.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface KnowledgeMapUiState {
    data object Idle : KnowledgeMapUiState
    data object Loading : KnowledgeMapUiState
    data class Success(val mapData: KnowledgeMapData) : KnowledgeMapUiState
    data class Error(val message: String) : KnowledgeMapUiState
}

class KnowledgeMapViewModel(
    private val aiService: GeminiAiService,
    private val bookmarkRepo: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<KnowledgeMapUiState>(KnowledgeMapUiState.Idle)
    val uiState: StateFlow<KnowledgeMapUiState> = _uiState.asStateFlow()

    private val _selectedNode = MutableStateFlow<MapNode?>(null)
    val selectedNode: StateFlow<MapNode?> = _selectedNode.asStateFlow()

    fun generateKnowledgeMap(topic: String, context: String) {
        viewModelScope.launch {
            _uiState.value = KnowledgeMapUiState.Loading
            val result = aiService.generateKnowledgeMap(topic, context)
            if (result.isSuccess) {
                _uiState.value = KnowledgeMapUiState.Success(result.getOrThrow())
            } else {
                _uiState.value = KnowledgeMapUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to generate Knowledge Map."
                )
            }
        }
    }

    fun selectNode(node: MapNode?) {
        _selectedNode.value = node
    }

    fun saveKnowledgeMap(topic: String, articleId: String) {
        viewModelScope.launch {
            bookmarkRepo.addBookmark(
                contentType = "KNOWLEDGE_MAP",
                contentId = articleId,
                title = "Knowledge Map: $topic",
                summary = "Interactive concept hierarchy for $topic",
                category = "Knowledge Map"
            )
        }
    }
}
