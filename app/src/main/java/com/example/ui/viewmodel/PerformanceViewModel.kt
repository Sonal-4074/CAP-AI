package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TopicPerformance
import com.example.data.repository.PerformanceRepository
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PerformanceOverview(
    val totalAttempted: Long,
    val overallAccuracy: Double,
    val totalCorrect: Long,
    val totalIncorrect: Long,
    val weakTopicsCount: Int,
    val strongTopicsCount: Int,
    val weakTopics: List<TopicPerformance>,
    val allTopics: List<TopicPerformance>
)

class PerformanceViewModel(
    private val performanceRepo: PerformanceRepository,
    private val quizRepo: QuizRepository
) : ViewModel() {

    val allPerformance: StateFlow<List<TopicPerformance>> = performanceRepo.performance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val overview: StateFlow<PerformanceOverview> = allPerformance.map { list ->
        val totalAttempted = list.sumOf { it.attempted }
        val totalCorrect = list.sumOf { it.correct }
        val totalIncorrect = list.sumOf { it.incorrect }
        val accuracy = if (totalAttempted > 0) (totalCorrect.toDouble() / totalAttempted.toDouble()) * 100.0 else 0.0

        val weak = list.filter { it.masteryLevel == "Weak" || it.accuracy < 50.0 }
        val strong = list.filter { it.masteryLevel == "Strong" || it.accuracy >= 85.0 }

        PerformanceOverview(
            totalAttempted = totalAttempted,
            overallAccuracy = accuracy,
            totalCorrect = totalCorrect,
            totalIncorrect = totalIncorrect,
            weakTopicsCount = weak.size,
            strongTopicsCount = strong.size,
            weakTopics = weak,
            allTopics = list
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        PerformanceOverview(0, 0.0, 0, 0, 0, 0, emptyList(), emptyList())
    )
}
