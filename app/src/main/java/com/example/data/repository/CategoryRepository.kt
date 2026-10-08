package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Category
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CategoryRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val categoriesCol = db.collection("categories")

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    fun observeCategories(): Flow<List<Category>> =
        categoriesCol
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
            }
            .catch { e ->
                Log.e("CategoryRepository", "Error observing categories", e)
                emit(emptyList())
            }

    suspend fun addCategory(category: Category): Result<String> {
        return try {
            requireUserId()
            val docRef = if (category.id.isNotEmpty()) categoriesCol.document(category.id) else categoriesCol.document()
            val id = docRef.id
            val payload = category.copy(id = id).toMap().toMutableMap()
            payload["createdAt"] = FieldValue.serverTimestamp()
            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Failed to add category", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            requireUserId()
            categoriesCol.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Failed to delete category", e)
            Result.failure(e)
        }
    }

    suspend fun seedCategoriesIfEmpty() {
        try {
            val snapshot = categoriesCol.limit(1).get().await()
            if (!snapshot.isEmpty) return

            val defaultCategories = listOf(
                Category(id = "cat_anime", name = "Anime", slug = "anime", iconUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=300", wallpaperCount = 42L),
                Category(id = "cat_nature", name = "Nature", slug = "nature", iconUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?w=300", wallpaperCount = 38L),
                Category(id = "cat_cars", name = "Cars", slug = "cars", iconUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=300", wallpaperCount = 29L),
                Category(id = "cat_gaming", name = "Gaming", slug = "gaming", iconUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=300", wallpaperCount = 54L),
                Category(id = "cat_space", name = "Space", slug = "space", iconUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=300", wallpaperCount = 26L),
                Category(id = "cat_abstract", name = "Abstract", slug = "abstract", iconUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300", wallpaperCount = 33L),
                Category(id = "cat_minimal", name = "Minimal", slug = "minimal", iconUrl = "https://images.unsplash.com/photo-1507499739999-097706ad8914?w=300", wallpaperCount = 18L),
                Category(id = "cat_fantasy", name = "Fantasy", slug = "fantasy", iconUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300", wallpaperCount = 24L),
                Category(id = "cat_animals", name = "Animals", slug = "animals", iconUrl = "https://images.unsplash.com/photo-1534188753412-3e26d0d618d6?w=300", wallpaperCount = 19L),
                Category(id = "cat_technology", name = "Technology", slug = "technology", iconUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=300", wallpaperCount = 22L)
            )

            for (c in defaultCategories) {
                categoriesCol.document(c.id).set(c.toMap().toMutableMap().apply {
                    put("createdAt", FieldValue.serverTimestamp())
                }).await()
            }
        } catch (e: Exception) {
            Log.w("CategoryRepository", "Failed to seed categories", e)
        }
    }
}
