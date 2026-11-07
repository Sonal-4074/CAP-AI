package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: String,
    val source: String,
    val publishedDate: String,
    val url: String = "",
    val imageUrl: String = "",
    val keyFactsJson: String = "",
    val examRelevance: String = "",
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val fetchTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val contentType: String,
    val contentId: String,
    val title: String,
    val summary: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val quizType: String,
    val topic: String,
    val targetExamination: String,
    val totalQuestions: Int,
    val attemptedQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val unattemptedQuestions: Int,
    val accuracy: Double,
    val timeTakenSeconds: Long,
    val score: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_performance")
data class PerformanceEntity(
    @PrimaryKey val topic: String,
    val attempted: Int,
    val correct: Int,
    val incorrect: Int,
    val accuracy: Double,
    val masteryLevel: String,
    val weakConceptsJson: String,
    val repeatedMistakes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_maps")
data class KnowledgeMapEntity(
    @PrimaryKey val id: String,
    val articleId: String,
    val mainTopic: String,
    val rawJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "revision_notes")
data class RevisionNoteEntity(
    @PrimaryKey val id: String,
    val topic: String,
    val conceptsJson: String,
    val summary: String,
    val keyFactsJson: String,
    val previousAccuracy: Double,
    val newAccuracy: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
