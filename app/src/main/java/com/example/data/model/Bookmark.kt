package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Bookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val contentType: String = "NEWS", // NEWS, KNOWLEDGE_MAP, QUESTION, REVISION
    val contentId: String = "",
    val title: String = "",
    val summary: String = "",
    val category: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
