package com.example.data.repository

import android.util.Log
import com.example.data.local.BookmarkEntity
import com.example.data.local.CapDao
import com.example.data.model.Bookmark
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "BookmarkRepository"

class BookmarkRepository(
    private val dao: CapDao,
    private val firestore: FirebaseFirestore
) {
    private val auth = Firebase.auth

    val bookmarks: Flow<List<Bookmark>> = dao.getAllBookmarks().map { list ->
        list.map { entity ->
            Bookmark(
                id = entity.id,
                userId = auth.currentUser?.uid ?: "",
                contentType = entity.contentType,
                contentId = entity.contentId,
                title = entity.title,
                summary = entity.summary,
                category = entity.category,
                createdAt = null
            )
        }
    }

    suspend fun addBookmark(
        contentType: String,
        contentId: String,
        title: String,
        summary: String,
        category: String
    ) = withContext(Dispatchers.IO) {
        val bookmarkId = UUID.randomUUID().toString()
        val entity = BookmarkEntity(
            id = bookmarkId,
            contentType = contentType,
            contentId = contentId,
            title = title,
            summary = summary,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        dao.insertBookmark(entity)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("bookmarks")
                    .document(bookmarkId)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "contentType" to contentType,
                            "contentId" to contentId,
                            "title" to title,
                            "summary" to summary,
                            "category" to category,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bookmark to Firestore: ${e.message}")
            }
        }
    }

    suspend fun removeBookmark(contentId: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkByContentId(contentId)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("bookmarks")
                    .whereEqualTo("contentId", contentId)
                    .get()
                    .await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting bookmark from Firestore: ${e.message}")
            }
        }
    }

    suspend fun isBookmarked(contentId: String): Boolean = withContext(Dispatchers.IO) {
        dao.isBookmarked(contentId)
    }
}
