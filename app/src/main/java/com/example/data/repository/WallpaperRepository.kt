package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Wallpaper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WallpaperRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val wallpapersCol = db.collection("wallpapers")

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    fun observeAllWallpapers(): Flow<List<Wallpaper>> =
        wallpapersCol
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing wallpapers", e)
                emit(emptyList())
            }

    fun observeAdminWallpapers(): Flow<List<Wallpaper>> =
        wallpapersCol
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing admin wallpapers", e)
                emit(emptyList())
            }

    fun observeFeaturedWallpapers(): Flow<List<Wallpaper>> =
        wallpapersCol
            .whereEqualTo("isPublished", true)
            .whereEqualTo("isFeatured", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing featured", e)
                emit(emptyList())
            }

    fun observeTrendingWallpapers(): Flow<List<Wallpaper>> =
        wallpapersCol
            .whereEqualTo("isPublished", true)
            .whereEqualTo("isTrending", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing trending", e)
                emit(emptyList())
            }

    fun observePopularWallpapers(): Flow<List<Wallpaper>> =
        wallpapersCol
            .whereEqualTo("isPublished", true)
            .whereEqualTo("isPopular", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing popular", e)
                emit(emptyList())
            }

    fun observeWallpapersByCategory(category: String): Flow<List<Wallpaper>> =
        wallpapersCol
            .whereEqualTo("isPublished", true)
            .whereEqualTo("category", category)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { e ->
                Log.e("WallpaperRepository", "Error observing category: $category", e)
                emit(emptyList())
            }

    suspend fun getWallpaperById(id: String): Wallpaper? {
        val doc = wallpapersCol.document(id).get().await()
        if (!doc.exists()) {
            return null
        }
        return doc.toObject(Wallpaper::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

    suspend fun addWallpaper(wallpaper: Wallpaper): Result<String> {
        return try {
            requireUserId()
            val docRef = if (wallpaper.id.isNotEmpty()) wallpapersCol.document(wallpaper.id) else wallpapersCol.document()
            val generatedId = docRef.id
            val payload = wallpaper.copy(id = generatedId).toMap().toMutableMap()
            payload["uploadedAt"] = FieldValue.serverTimestamp()
            payload["uploaderUid"] = auth.currentUser?.uid.orEmpty()
            docRef.set(payload).await()
            Result.success(generatedId)
        } catch (e: Exception) {
            Log.e("WallpaperRepository", "Failed to add wallpaper", e)
            Result.failure(e)
        }
    }

    suspend fun updateWallpaper(wallpaper: Wallpaper): Result<Unit> {
        return try {
            requireUserId()
            val payload = wallpaper.toMap().toMutableMap()
            payload["updatedAt"] = FieldValue.serverTimestamp()
            wallpapersCol.document(wallpaper.id).update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WallpaperRepository", "Failed to update wallpaper", e)
            Result.failure(e)
        }
    }

    suspend fun deleteWallpaper(id: String): Result<Unit> {
        return try {
            requireUserId()
            wallpapersCol.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WallpaperRepository", "Failed to delete wallpaper", e)
            Result.failure(e)
        }
    }

    suspend fun incrementDownloadCount(id: String) {
        try {
            wallpapersCol.document(id).update("downloadCount", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            Log.w("WallpaperRepository", "Failed to increment download count", e)
        }
    }

    suspend fun incrementFavoriteCount(id: String, delta: Long) {
        try {
            wallpapersCol.document(id).update("favoriteCount", FieldValue.increment(delta)).await()
        } catch (e: Exception) {
            Log.w("WallpaperRepository", "Failed to increment favorite count", e)
        }
    }

    suspend fun seedInitialWallpapersIfEmpty() {
        try {
            val snapshot = wallpapersCol.limit(1).get().await()
            if (!snapshot.isEmpty) return

            val curations = listOf(
                Wallpaper(
                    id = "wp_neon_anime_01",
                    title = "Cyber City Angel",
                    description = "Vibrant cyberpunk neon anime girl overlooking a glowing metropolis.",
                    category = "Anime",
                    tags = listOf("Anime", "Cyberpunk", "Neon", "Sci-Fi"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_anime_01",
                    thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 1420L,
                    favoriteCount = 389L,
                    isFeatured = true,
                    isTrending = true,
                    isPopular = true,
                    isPublished = true
                ),
                Wallpaper(
                    id = "wp_nature_mountain_02",
                    title = "Nordic Aurora Mist",
                    description = "Emerald northern lights dancing across jagged snow-capped peaks and fjord reflections.",
                    category = "Nature",
                    tags = listOf("Nature", "Aurora", "Mountains", "Night"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_nature_02",
                    thumbnailUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 954L,
                    favoriteCount = 281L,
                    isFeatured = true,
                    isTrending = true,
                    isPopular = false,
                    isPublished = true
                ),
                Wallpaper(
                    id = "wp_space_galaxy_03",
                    title = "Cosmic Nebulae Horizon",
                    description = "Deep violet and sapphire starlight bursting through interstellar cosmic dust.",
                    category = "Space",
                    tags = listOf("Space", "Galaxy", "Cosmic", "Universe"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_space_03",
                    thumbnailUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 2105L,
                    favoriteCount = 740L,
                    isFeatured = false,
                    isTrending = true,
                    isPopular = true,
                    isPublished = true
                ),
                Wallpaper(
                    id = "wp_cars_gt_04",
                    title = "Hypercar Phantom Track",
                    description = "Matte midnight carbon fiber supercar with radiant red aerodynamic lines.",
                    category = "Cars",
                    tags = listOf("Cars", "Supercar", "Speed", "Luxury"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_cars_04",
                    thumbnailUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 820L,
                    favoriteCount = 195L,
                    isFeatured = false,
                    isTrending = false,
                    isPopular = true,
                    isPublished = true
                ),
                Wallpaper(
                    id = "wp_abstract_glass_05",
                    title = "Liquid Crystal Prism",
                    description = "Flowing iridescent glass waves refracting prismatic turquoise and magenta light.",
                    category = "Abstract",
                    tags = listOf("Abstract", "Glass", "3D", "Clean"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_abstract_05",
                    thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 1880L,
                    favoriteCount = 612L,
                    isFeatured = true,
                    isTrending = false,
                    isPopular = true,
                    isPublished = true
                ),
                Wallpaper(
                    id = "wp_gaming_warrior_06",
                    title = "Cyber Samurai Edge",
                    description = "Futuristic blade master with glowing cyan katana in a rain-soaked neon alley.",
                    category = "Gaming",
                    tags = listOf("Gaming", "Samurai", "Cyberpunk", "Blade"),
                    cloudinaryUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=1200&q=80",
                    cloudinaryPublicId = "root_dr/sample_gaming_06",
                    thumbnailUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=600&q=80",
                    width = 1200L,
                    height = 2000L,
                    downloadCount = 1350L,
                    favoriteCount = 445L,
                    isFeatured = true,
                    isTrending = true,
                    isPopular = false,
                    isPublished = true
                )
            )

            for (w in curations) {
                wallpapersCol.document(w.id).set(w.toMap().toMutableMap().apply {
                    put("uploadedAt", FieldValue.serverTimestamp())
                }).await()
            }
        } catch (e: Exception) {
            Log.w("WallpaperRepository", "Error seeding initial wallpapers", e)
        }
    }
}
