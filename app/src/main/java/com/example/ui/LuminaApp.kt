package com.example.ui

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.repository.CategoryRepository
import com.example.data.repository.UserInteractionsRepository
import com.example.data.repository.WallpaperRepository
import com.example.ui.components.GlassBottomNavigation
import com.example.ui.components.NavTab
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WallpaperDetailScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LuminaApp() {
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        LuminaMainContent(currentUserId = user.uid, currentUser = user)
    }
}

@Composable
private fun LuminaMainContent(
    currentUserId: String,
    currentUser: FirebaseUser
) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    val db = remember(context) {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val wallpaperRepo = remember(db) { WallpaperRepository(db) }
    val categoryRepo = remember(db) { CategoryRepository(db) }
    val userInteractionsRepo = remember(db) { UserInteractionsRepository(db) }

    val viewModel: LuminaAppViewModel = viewModel(
        key = currentUserId,
        factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer {
                LuminaAppViewModel(wallpaperRepo, categoryRepo, userInteractionsRepo, currentUserId)
            }
        }
    )

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val adminWallpapers by viewModel.adminWallpapers.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteWallpapers by viewModel.favoriteWallpapers.collectAsStateWithLifecycle()
    val downloadedWallpapers by viewModel.downloadedWallpapers.collectAsStateWithLifecycle()
    val historyWallpapers by viewModel.historyWallpapers.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            if (screenState is ScreenState.Main) {
                val currentTab = (screenState as ScreenState.Main).tab
                GlassBottomNavigation(
                    selectedTab = currentTab,
                    onTabSelected = { tab -> viewModel.navigateToTab(tab) },
                    isAdmin = isAdmin
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Crossfade(targetState = screenState, label = "screen_crossfade") { state ->
                when (state) {
                    is ScreenState.Main -> {
                        when (state.tab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    userName = userProfile?.displayName ?: currentUser.displayName.orEmpty(),
                                    userPhotoUrl = userProfile?.photoUrl ?: currentUser.photoUrl?.toString().orEmpty(),
                                    wallpapers = wallpapers,
                                    favoriteIds = favoriteIds,
                                    isLoading = wallpapers.isEmpty(),
                                    onWallpaperClick = { wp -> viewModel.openWallpaper(wp) },
                                    onFavoriteToggle = { wp -> viewModel.toggleFavorite(wp) },
                                    onProfileClick = { viewModel.navigateToTab(NavTab.PROFILE) }
                                )
                            }
                            NavTab.CATEGORIES -> {
                                CategoriesScreen(
                                    categories = categories,
                                    allWallpapers = wallpapers,
                                    favoriteIds = favoriteIds,
                                    onWallpaperClick = { wp -> viewModel.openWallpaper(wp) },
                                    onFavoriteToggle = { wp -> viewModel.toggleFavorite(wp) }
                                )
                            }
                            NavTab.FAVORITES -> {
                                FavoritesScreen(
                                    favoriteWallpapers = favoriteWallpapers,
                                    onWallpaperClick = { wp -> viewModel.openWallpaper(wp) },
                                    onFavoriteToggle = { wp -> viewModel.toggleFavorite(wp) },
                                    onExploreClick = { viewModel.navigateToTab(NavTab.HOME) }
                                )
                            }
                            NavTab.PROFILE -> {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    isAdmin = isAdmin,
                                    downloadedWallpapers = downloadedWallpapers,
                                    historyWallpapers = historyWallpapers,
                                    onWallpaperClick = { wp -> viewModel.openWallpaper(wp) },
                                    onOpenAdminDashboard = { viewModel.navigateToTab(NavTab.ADMIN) },
                                    onSignOut = {
                                        viewModel.signOut(context, credentialManager) {}
                                    }
                                )
                            }
                            NavTab.ADMIN -> {
                                AdminDashboardScreen(
                                    wallpapers = adminWallpapers,
                                    categories = categories,
                                    users = allUsers,
                                    onAddWallpaper = { wp -> viewModel.addWallpaper(wp) },
                                    onUpdateWallpaper = { wp -> viewModel.updateWallpaper(wp) },
                                    onDeleteWallpaper = { id -> viewModel.deleteWallpaper(id) },
                                    onAddCategory = { name, icon -> viewModel.addCategory(name, icon) },
                                    onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                                    onToggleBlockUser = { uid, block -> viewModel.toggleBlockUser(uid, block) },
                                    onDeleteUser = { uid -> viewModel.deleteUser(uid) },
                                    onBack = { viewModel.navigateToTab(NavTab.HOME) }
                                )
                            }
                        }
                    }
                    is ScreenState.Detail -> {
                        val currentWallpaper = state.wallpaper
                        val relatedWallpapers = remember(wallpapers, currentWallpaper) {
                            wallpapers.filter { it.category == currentWallpaper.category && it.id != currentWallpaper.id }
                        }
                        WallpaperDetailScreen(
                            wallpaper = currentWallpaper,
                            relatedWallpapers = relatedWallpapers,
                            isFavorite = favoriteIds.contains(currentWallpaper.id),
                            onBack = { viewModel.navigateBack() },
                            onFavoriteToggle = { viewModel.toggleFavorite(currentWallpaper) },
                            onDownload = { viewModel.recordDownload(currentWallpaper) },
                            onRelatedClick = { related -> viewModel.openWallpaper(related) }
                        )
                    }
                    is ScreenState.Admin -> {
                        AdminDashboardScreen(
                            wallpapers = adminWallpapers,
                            categories = categories,
                            users = allUsers,
                            onAddWallpaper = { wp -> viewModel.addWallpaper(wp) },
                            onUpdateWallpaper = { wp -> viewModel.updateWallpaper(wp) },
                            onDeleteWallpaper = { id -> viewModel.deleteWallpaper(id) },
                            onAddCategory = { name, icon -> viewModel.addCategory(name, icon) },
                            onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                            onToggleBlockUser = { uid, block -> viewModel.toggleBlockUser(uid, block) },
                            onDeleteUser = { uid -> viewModel.deleteUser(uid) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
