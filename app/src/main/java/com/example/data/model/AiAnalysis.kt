package com.example.data.model

data class AiAnalysis(
    val articleId: String,
    val simpleExplanation: String,
    val keyFacts: List<String>,
    val examRelevance: String,
    val keywords: List<KeywordDefinition>,
    val backgroundInfo: String,
    val possibleExamQuestions: List<String>,
    val quickRevision: String
)

data class KeywordDefinition(
    val term: String,
    val definition: String
)
