package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CapDao {

    // Articles
    @Query("SELECT * FROM articles ORDER BY fetchTimestamp DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY fetchTimestamp DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, bookmarked: Boolean)

    @Query("UPDATE articles SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE contentType = :type ORDER BY timestamp DESC")
    fun getBookmarksByType(type: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE contentId = :contentId")
    suspend fun deleteBookmarkByContentId(contentId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE contentId = :contentId)")
    suspend fun isBookmarked(contentId: String): Boolean

    // Quiz Attempts
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    // Topic Performance
    @Query("SELECT * FROM topic_performance ORDER BY accuracy ASC")
    fun getAllPerformance(): Flow<List<PerformanceEntity>>

    @Query("SELECT * FROM topic_performance WHERE topic = :topic LIMIT 1")
    suspend fun getPerformanceForTopic(topic: String): PerformanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePerformance(perf: PerformanceEntity)

    // Knowledge Maps
    @Query("SELECT * FROM knowledge_maps WHERE articleId = :articleId LIMIT 1")
    suspend fun getKnowledgeMapForArticle(articleId: String): KnowledgeMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeMap(map: KnowledgeMapEntity)

    // Revision Notes
    @Query("SELECT * FROM revision_notes ORDER BY timestamp DESC")
    fun getAllRevisionNotes(): Flow<List<RevisionNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionNote(note: RevisionNoteEntity)

    @Query("UPDATE revision_notes SET newAccuracy = :newAcc, status = :status WHERE id = :id")
    suspend fun updateRevisionStatus(id: String, newAcc: Double, status: String)
}
