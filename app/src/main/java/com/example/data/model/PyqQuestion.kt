package com.example.data.model

data class PyqQuestion(
    val id: String,
    val examination: String, // UPSC, SSC CGL, Banking, Railway, State PSC
    val year: Int,
    val subject: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val sourceReference: String,
    val difficulty: String = "Medium"
)
