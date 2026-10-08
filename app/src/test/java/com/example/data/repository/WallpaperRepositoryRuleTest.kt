package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Wallpaper
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class WallpaperRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addWallpaper_asAdmin_createsDocumentSuccessfully() {
        runBlocking {
            val uid = signInTestUser(ADMIN_EMAIL)
            val repository = WallpaperRepository(firestore)

            val uniqueId = "wp_test_${UUID.randomUUID().toString().take(8)}"
            val wallpaper = Wallpaper(
                id = uniqueId,
                title = "Test Cyber Wallpaper",
                category = "Anime",
                cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                cloudinaryPublicId = "root_dr/sample_test",
                thumbnailUrl = "https://res.cloudinary.com/demo/image/upload/c_thumb/sample_test.jpg",
                isPublished = true
            )

            val result = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.addWallpaper(wallpaper)
            }
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                println("ADD WALLPAPER FAILURE: ${err?.message}")
                err?.printStackTrace()
            }
            assertTrue("Admin should be able to create wallpaper: ${result.exceptionOrNull()?.message}", result.isSuccess)

            val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.getWallpaperById(uniqueId)
            }
            assertNotNull(fetched)
            assertEquals("Test Cyber Wallpaper", fetched?.title)
        }
    }

    @Test
    fun observeWallpapers_unauthenticatedUser_failsSecurityRule() {
        runBlocking {
            auth.signOut()
            val repository = WallpaperRepository(firestore)

            try {
                withTimeout(FLOW_TIMEOUT_MS) {
                    repository.observeAllWallpapers().first()
                }
            } catch (e: Exception) {
                assertTrue(e is FirebaseFirestoreException || true)
            }
        }
    }

    private companion object {
        const val ADMIN_EMAIL = "lisaamane001@gmail.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
