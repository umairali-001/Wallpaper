package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Wallpaper(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Anime",
    val tags: List<String> = emptyList(),
    val cloudinaryUrl: String = "",
    val cloudinaryPublicId: String = "",
    val thumbnailUrl: String = "",
    val width: Long = 1080L,
    val height: Long = 1920L,
    val aspectRatio: String = "9:16",
    @ServerTimestamp
    val uploadedAt: Timestamp? = null,
    val downloadCount: Long = 0L,
    val favoriteCount: Long = 0L,
    @get:PropertyName("isFeatured") @set:PropertyName("isFeatured")
    var isFeatured: Boolean = false,
    @get:PropertyName("isTrending") @set:PropertyName("isTrending")
    var isTrending: Boolean = false,
    @get:PropertyName("isPopular") @set:PropertyName("isPopular")
    var isPopular: Boolean = false,
    @get:PropertyName("isPublished") @set:PropertyName("isPublished")
    var isPublished: Boolean = true,
    val uploaderUid: String = ""
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "tags" to tags,
            "cloudinaryUrl" to cloudinaryUrl,
            "cloudinaryPublicId" to cloudinaryPublicId,
            "thumbnailUrl" to thumbnailUrl,
            "width" to width,
            "height" to height,
            "aspectRatio" to aspectRatio,
            "downloadCount" to downloadCount,
            "favoriteCount" to favoriteCount,
            "isFeatured" to isFeatured,
            "isTrending" to isTrending,
            "isPopular" to isPopular,
            "isPublished" to isPublished,
            "uploaderUid" to uploaderUid
        )
        if (uploadedAt != null) {
            map["uploadedAt"] = uploadedAt
        }
        return map
    }
}
