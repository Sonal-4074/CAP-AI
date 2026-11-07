package com.example.data.model

data class Article(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: String,
    val source: String,
    val publishedDate: String,
    val url: String = "",
    val imageUrl: String = "",
    val keyFacts: List<String> = emptyList(),
    val examRelevance: String = "",
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false
)
