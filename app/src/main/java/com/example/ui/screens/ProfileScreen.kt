package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.model.Wallpaper
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaCyanVariant
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaGold
import com.example.ui.theme.LuminaNeonBlue
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaTextPrimary
import com.example.ui.theme.LuminaTextSecondary

enum class ProfileTab {
    DOWNLOADS, HISTORY
}

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    isAdmin: Boolean,
    downloadedWallpapers: List<Wallpaper>,
    historyWallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ProfileTab.DOWNLOADS) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080F16),
                        Color(0xFF0D1822),
                        Color(0xFF091118)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // User Profile Glass Header
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(14.dp, CircleShape, spotColor = LuminaCyan.copy(alpha = 0.5f))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(LuminaCyan, LuminaCyanVariant)
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile?.photoUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = userProfile.photoUrl,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = userProfile?.displayName?.take(1)?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile?.displayName ?: "Explorer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = userProfile?.email ?: "",
                            fontSize = 13.sp,
                            color = LuminaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pro / Admin Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(LuminaNeonBlue.copy(alpha = 0.15f))
                                .border(1.dp, LuminaCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isAdmin) LuminaCyan else LuminaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdmin) "Super Administrator" else "Pro Member • Active",
                                color = if (isAdmin) LuminaCyan else LuminaGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stat counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox(title = "Downloads", count = "${downloadedWallpapers.size}")
                            StatBox(title = "History", count = "${historyWallpapers.size}")
                            StatBox(title = "Credits", count = "${userProfile?.credits ?: 100}")
                        }
                    }
                }
            }

            // Admin Dashboard Shortcut Card (if Admin)
            if (isAdmin) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = LuminaCyan.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAdminDashboard() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LuminaCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = LuminaCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Admin Control Panel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = LuminaTextPrimary
                                    )
                                    Text(
                                        text = "Upload wallpapers, manage users & Cloudinary",
                                        fontSize = 12.sp,
                                        color = LuminaTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "Open →",
                                color = LuminaCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Tab Selector: Downloads vs Recently Viewed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassPill(
                        text = "Downloads (${downloadedWallpapers.size})",
                        icon = Icons.Default.Download,
                        isSelected = selectedTab == ProfileTab.DOWNLOADS,
                        onClick = { selectedTab = ProfileTab.DOWNLOADS },
                        modifier = Modifier.weight(1f)
                    )

                    GlassPill(
                        text = "Recent History (${historyWallpapers.size})",
                        icon = Icons.Default.History,
                        isSelected = selectedTab == ProfileTab.HISTORY,
                        onClick = { selectedTab = ProfileTab.HISTORY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // List of items in active tab
            val currentList = if (selectedTab == ProfileTab.DOWNLOADS) downloadedWallpapers else historyWallpapers
            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == ProfileTab.DOWNLOADS) "No downloads yet" else "No recent view history",
                            color = LuminaTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(currentList, key = { it.id }) { wp ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWallpaperClick(wp) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = wp.thumbnailUrl.ifEmpty { wp.cloudinaryUrl },
                                    contentDescription = wp.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wp.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = LuminaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${wp.category} • ${wp.aspectRatio}",
                                    fontSize = 12.sp,
                                    color = LuminaCyan
                                )
                            }
                        }
                    }
                }
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                GlassButton(
                    text = "Sign Out",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    isPrimary = false,
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "profile_signout_button"
                )
            }
        }
    }
}

@Composable
private fun StatBox(title: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LuminaTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = LuminaTextMuted
        )
    }
}
