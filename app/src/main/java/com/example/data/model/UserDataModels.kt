package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "user",
    val isBlocked: Boolean = false,
    val credits: Long = 100L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "role" to role,
            "isBlocked" to isBlocked,
            "credits" to credits
        )
        if (createdAt != null) map["createdAt"] = createdAt
        if (updatedAt != null) map["updatedAt"] = updatedAt
        return map
    }
}

data class FavoriteItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

data class DownloadItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

data class HistoryRecord(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    @ServerTimestamp
    val viewedAt: Timestamp? = null
)
