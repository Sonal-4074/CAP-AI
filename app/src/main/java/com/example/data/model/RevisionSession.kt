package com.example.data.model

data class RevisionSession(
    val topic: String,
    val concepts: List<String>,
    val explanation: String,
    val keyFacts: List<String>,
    val questions: List<ExamQuestion>,
    val previousAccuracy: Double,
    val currentAccuracy: Double = 0.0,
    val masteryUpdated: Boolean = false
)
