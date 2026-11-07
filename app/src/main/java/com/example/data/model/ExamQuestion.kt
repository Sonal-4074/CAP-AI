package com.example.data.model

data class ExamQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: String, // Easy, Medium, Hard
    val category: String,
    val relatedConcept: String,
    val examType: String = "General"
)
