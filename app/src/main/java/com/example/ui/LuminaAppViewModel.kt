package com.example.ui

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.UserProfile
import com.example.data.model.Wallpaper
import com.example.data.repository.CategoryRepository
import com.example.data.repository.UserInteractionsRepository
import com.example.data.repository.WallpaperRepository
import com.example.ui.components.NavTab
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScreenState {
    data class Main(val tab: NavTab = NavTab.HOME) : ScreenState
    data class Detail(val wallpaper: Wallpaper) : ScreenState
    data object Admin : ScreenState
}

class LuminaAppViewModel(
    private val wallpaperRepo: WallpaperRepository,
    private val categoryRepo: CategoryRepository,
    private val userInteractionsRepo: UserInteractionsRepository,
    private val currentUserId: String
) : ViewModel() {

    private val auth = Firebase.auth

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Main(NavTab.HOME))
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val wallpapers: StateFlow<List<Wallpaper>> = wallpaperRepo.observeAllWallpapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminWallpapers: StateFlow<List<Wallpaper>> = wallpaperRepo.observeAdminWallpapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<Set<String>> = userInteractionsRepo.observeFavorites(currentUserId)
        .combine(MutableStateFlow(Unit)) { favs, _ -> favs.map { it.wallpaperId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteWallpapers: StateFlow<List<Wallpaper>> = combine(wallpapers, favoriteIds) { all, favs ->
        all.filter { favs.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedWallpapers: StateFlow<List<Wallpaper>> = userInteractionsRepo.observeDownloads(currentUserId)
        .combine(wallpapers) { downloads, allWps ->
            val downloadedIds = downloads.map { it.wallpaperId }.toSet()
            allWps.filter { downloadedIds.contains(it.id) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyWallpapers: StateFlow<List<Wallpaper>> = userInteractionsRepo.observeHistory(currentUserId)
        .combine(wallpapers) { history, allWps ->
            val historyIds = history.map { it.wallpaperId }.toSet()
            allWps.filter { historyIds.contains(it.id) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserProfile>> = userInteractionsRepo.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            auth.currentUser?.let { user ->
                val profile = userInteractionsRepo.syncUserProfile(user)
                _userProfile.value = profile
                val adminCheck = userInteractionsRepo.checkIsAdmin(user)
                _isAdmin.value = adminCheck
            }
            // Seed initial wallpapers and categories if Firestore collections are empty
            categoryRepo.seedCategoriesIfEmpty()
            wallpaperRepo.seedInitialWallpapersIfEmpty()
        }
    }

    fun navigateToTab(tab: NavTab) {
        if (tab == NavTab.ADMIN) {
            _screenState.value = ScreenState.Admin
        } else {
            _screenState.value = ScreenState.Main(tab)
        }
    }

    fun openWallpaper(wallpaper: Wallpaper) {
        _screenState.value = ScreenState.Detail(wallpaper)
        viewModelScope.launch {
            userInteractionsRepo.recordHistory(currentUserId, wallpaper.id)
        }
    }

    fun navigateBack() {
        _screenState.value = ScreenState.Main(NavTab.HOME)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            val wasFav = favoriteIds.value.contains(wallpaper.id)
            userInteractionsRepo.toggleFavorite(currentUserId, wallpaper.id)
            wallpaperRepo.incrementFavoriteCount(wallpaper.id, if (wasFav) -1L else 1L)
        }
    }

    fun recordDownload(wallpaper: Wallpaper) {
        viewModelScope.launch {
            userInteractionsRepo.recordDownload(currentUserId, wallpaper.id)
            wallpaperRepo.incrementDownloadCount(wallpaper.id)
        }
    }

    fun addWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            wallpaperRepo.addWallpaper(wallpaper)
        }
    }

    fun updateWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            wallpaperRepo.updateWallpaper(wallpaper)
        }
    }

    fun deleteWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            wallpaperRepo.deleteWallpaper(wallpaperId)
        }
    }

    fun addCategory(name: String, iconUrl: String) {
        viewModelScope.launch {
            categoryRepo.addCategory(Category(name = name, slug = name.lowercase().replace(" ", "_"), iconUrl = iconUrl))
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepo.deleteCategory(categoryId)
        }
    }

    fun toggleBlockUser(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            userInteractionsRepo.toggleBlockUser(userId, isBlocked)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userInteractionsRepo.deleteUser(userId)
        }
    }

    fun signOut(context: Context, credentialManager: CredentialManager, onComplete: () -> Unit) {
        auth.signOut()
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("LuminaAppVM", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
