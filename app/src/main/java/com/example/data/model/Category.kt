package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Category(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val iconUrl: String = "",
    val wallpaperCount: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "slug" to slug,
            "iconUrl" to iconUrl,
            "wallpaperCount" to wallpaperCount
        )
        if (createdAt != null) map["createdAt"] = createdAt
        if (updatedAt != null) map["updatedAt"] = updatedAt
        return map
    }
}
