package com.example.ui.screens

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.Wallpaper
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.ShimmerBox
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaCyanVariant
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaNeonBlue
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaTextPrimary
import com.example.ui.theme.LuminaTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperDetailScreen(
    wallpaper: Wallpaper,
    relatedWallpapers: List<Wallpaper>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownload: () -> Unit,
    onRelatedClick: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showSetDialog by remember { mutableStateOf(false) }
    var isSettingWallpaper by remember { mutableStateOf(false) }

    // Zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3.5f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // High-res Background Image
        val imageUrl = wallpaper.cloudinaryUrl.ifEmpty { wallpaper.thumbnailUrl }

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            loading = {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            }
        )

        // Top Navigation Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        contentDescription = "Back",
                        testTag = "detail_back_button"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassIconButton(
                            icon = Icons.Default.Info,
                            onClick = { showInfoSheet = !showInfoSheet },
                            contentDescription = "Wallpaper info",
                            testTag = "detail_info_button"
                        )

                        GlassIconButton(
                            icon = Icons.Default.Share,
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, wallpaper.title)
                                    putExtra(Intent.EXTRA_TEXT, "Check out this wallpaper '${wallpaper.title}' on Lumina: $imageUrl")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
                            },
                            contentDescription = "Share",
                            testTag = "detail_share_button"
                        )
                    }
                }
            }
        }

        // Bottom Action Dock
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Wallpaper Title & Category Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallpaper.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${wallpaper.category} • ${wallpaper.aspectRatio} • ${wallpaper.width}x${wallpaper.height}",
                            fontSize = 12.sp,
                            color = LuminaCyan
                        )
                    }

                    // Favorite Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(LuminaGlassCard)
                            .border(1.dp, LuminaGlassBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, color = LuminaCoral)
                            ) { onFavoriteToggle() }
                            .testTag("detail_favorite_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) LuminaCoral else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Set Wallpaper & Download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Set Wallpaper Button (Main Action)
                    GlassButton(
                        text = if (isSettingWallpaper) "Applying..." else "Set Wallpaper",
                        icon = Icons.Default.Wallpaper,
                        isLoading = isSettingWallpaper,
                        onClick = { showSetDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "detail_set_wallpaper_button"
                    )

                    // Download Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LuminaGlassCard)
                            .border(1.dp, LuminaGlassBorder, RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = LuminaCyan)
                            ) {
                                onDownload()
                                Toast.makeText(context, "Wallpaper downloaded to device", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("detail_download_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = LuminaCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Related Wallpapers Preview Row
                if (relatedWallpapers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Related in ${wallpaper.category}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaTextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(relatedWallpapers.take(6)) { related ->
                            Box(
                                modifier = Modifier
                                    .size(width = 65.dp, height = 95.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, LuminaGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { onRelatedClick(related) }
                            ) {
                                SubcomposeAsyncImage(
                                    model = related.thumbnailUrl.ifEmpty { related.cloudinaryUrl },
                                    contentDescription = related.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info Details Dialog / Bottom Sheet
        if (showInfoSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showInfoSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Wallpaper Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (wallpaper.description.isNotEmpty()) {
                            Text(
                                text = wallpaper.description,
                                fontSize = 13.sp,
                                color = LuminaTextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        DetailRow("Category", wallpaper.category)
                        DetailRow("Resolution", "${wallpaper.width} x ${wallpaper.height} px")
                        DetailRow("Aspect Ratio", wallpaper.aspectRatio)
                        DetailRow("Downloads", "${wallpaper.downloadCount}")
                        DetailRow("Favorites", "${wallpaper.favoriteCount}")

                        if (wallpaper.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Tags", color = LuminaTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                wallpaper.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LuminaNeonBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("#$tag", color = LuminaCyan, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set Wallpaper Choice Dialog
        if (showSetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showSetDialog = false },
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Apply Wallpaper",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select screen destination:",
                            fontSize = 13.sp,
                            color = LuminaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        SetOptionItem(title = "Home Screen", subtitle = "Apply only to desktop launcher") {
                            showSetDialog = false
                            isSettingWallpaper = true
                            applyWallpaper(context, imageUrl, WallpaperManager.FLAG_SYSTEM, scope) {
                                isSettingWallpaper = false
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SetOptionItem(title = "Lock Screen", subtitle = "Apply only to lock screen") {
                            showSetDialog = false
                            isSettingWallpaper = true
                            applyWallpaper(context, imageUrl, WallpaperManager.FLAG_LOCK, scope) {
                                isSettingWallpaper = false
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SetOptionItem(title = "Both Screens", subtitle = "Set on Home & Lock screen") {
                            showSetDialog = false
                            isSettingWallpaper = true
                            applyWallpaper(context, imageUrl, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK, scope) {
                                isSettingWallpaper = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = LuminaTextMuted, fontSize = 12.sp)
        Text(text = value, color = LuminaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SetOptionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LuminaGlassCard)
            .border(1.dp, LuminaGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = LuminaCyan)
            ) { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(title, color = LuminaCyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = LuminaTextMuted, fontSize = 11.sp)
        }
    }
}

private fun applyWallpaper(
    context: Context,
    imageUrl: String,
    flag: Int,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, flag)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Wallpaper successfully applied!", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Unable to load image for wallpaper", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
    }
}
