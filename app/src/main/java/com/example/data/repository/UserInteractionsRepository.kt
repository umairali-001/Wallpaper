package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.DownloadItem
import com.example.data.model.FavoriteItem
import com.example.data.model.HistoryRecord
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserInteractionsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val favoritesCol = db.collection("favorites")
    private val downloadsCol = db.collection("downloads")
    private val historyCol = db.collection("history")
    private val usersCol = db.collection("users")
    private val adminsCol = db.collection("admins")

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    suspend fun checkIsAdmin(user: FirebaseUser?): Boolean {
        if (user == null) return false
        if (user.email == "lisaamane001@gmail.com") return true
        return try {
            val adminDoc = adminsCol.document(user.uid).get().await()
            adminDoc.exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun syncUserProfile(user: FirebaseUser): UserProfile {
        val userDoc = usersCol.document(user.uid)
        return try {
            val snapshot = userDoc.get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(userId = user.uid, email = user.email.orEmpty(), displayName = user.displayName.orEmpty())
                profile
            } else {
                val isOwnerAdmin = user.email == "lisaamane001@gmail.com"
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    role = if (isOwnerAdmin) "admin" else "user",
                    isBlocked = false,
                    credits = 100
                )
                val payload = profile.toMap().toMutableMap()
                payload["createdAt"] = FieldValue.serverTimestamp()
                userDoc.set(payload).await()

                if (isOwnerAdmin) {
                    adminsCol.document(user.uid).set(mapOf(
                        "userId" to user.uid,
                        "email" to user.email.orEmpty(),
                        "role" to "superadmin",
                        "createdAt" to FieldValue.serverTimestamp()
                    )).await()
                }

                profile
            }
        } catch (e: Exception) {
            Log.e("UserInteractionsRepo", "Error syncing user profile", e)
            UserProfile(userId = user.uid, email = user.email.orEmpty(), displayName = user.displayName.orEmpty())
        }
    }

    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> =
        favoritesCol
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(FavoriteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { emit(emptyList()) }

    suspend fun isFavorite(userId: String, wallpaperId: String): Boolean {
        return try {
            val docId = "${userId}_$wallpaperId"
            val doc = favoritesCol.document(docId).get().await()
            doc.exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(userId: String, wallpaperId: String): Boolean {
        requireUserId()
        val docId = "${userId}_$wallpaperId"
        val docRef = favoritesCol.document(docId)
        val doc = docRef.get().await()
        return if (doc.exists()) {
            docRef.delete().await()
            false
        } else {
            val payload = mapOf(
                "id" to docId,
                "userId" to userId,
                "wallpaperId" to wallpaperId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true
        }
    }

    fun observeDownloads(userId: String): Flow<List<DownloadItem>> =
        downloadsCol
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(DownloadItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { emit(emptyList()) }

    suspend fun recordDownload(userId: String, wallpaperId: String) {
        try {
            requireUserId()
            val docId = "${userId}_${wallpaperId}_${System.currentTimeMillis()}"
            val payload = mapOf(
                "id" to docId,
                "userId" to userId,
                "wallpaperId" to wallpaperId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            downloadsCol.document(docId).set(payload).await()
        } catch (e: Exception) {
            Log.w("UserInteractionsRepo", "Failed to record download", e)
        }
    }

    fun observeHistory(userId: String): Flow<List<HistoryRecord>> =
        historyCol
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(HistoryRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { emit(emptyList()) }

    suspend fun recordHistory(userId: String, wallpaperId: String) {
        try {
            requireUserId()
            val docId = "${userId}_$wallpaperId"
            val payload = mapOf(
                "id" to docId,
                "userId" to userId,
                "wallpaperId" to wallpaperId,
                "viewedAt" to FieldValue.serverTimestamp()
            )
            historyCol.document(docId).set(payload).await()
        } catch (e: Exception) {
            Log.w("UserInteractionsRepo", "Failed to record history", e)
        }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> =
        usersCol
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { emit(emptyList()) }

    suspend fun toggleBlockUser(userId: String, isBlocked: Boolean): Result<Unit> {
        return try {
            requireUserId()
            usersCol.document(userId).update("isBlocked", isBlocked).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            requireUserId()
            usersCol.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
