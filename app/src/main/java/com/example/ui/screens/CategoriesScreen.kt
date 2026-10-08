package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.Wallpaper
import com.example.ui.components.GlassIconButton
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaNeonBlue
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaTextPrimary
import com.example.ui.theme.LuminaTextSecondary

@Composable
fun CategoriesScreen(
    categories: List<Category>,
    allWallpapers: List<Wallpaper>,
    favoriteIds: Set<String>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

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
        if (selectedCategory == null) {
            // Categories Overview Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = LuminaCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wallpaper Categories",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuminaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Browse curated styles and visual genres",
                            fontSize = 13.sp,
                            color = LuminaTextSecondary
                        )
                    }
                }

                items(categories, key = { it.id }) { cat ->
                    val shape = RoundedCornerShape(22.dp)
                    val matchingCount = allWallpapers.count { it.category.equals(cat.name, ignoreCase = true) }
                    val displayCount = if (matchingCount > 0) matchingCount else cat.wallpaperCount

                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .shadow(12.dp, shape, spotColor = LuminaNeonBlue.copy(alpha = 0.25f))
                            .clip(shape)
                            .background(LuminaGlassCard)
                            .border(1.dp, LuminaGlassBorder, shape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = LuminaCyan)
                            ) { selectedCategory = cat }
                            .testTag("category_card_${cat.slug}")
                    ) {
                        if (cat.iconUrl.isNotEmpty()) {
                            AsyncImage(
                                model = cat.iconUrl,
                                contentDescription = cat.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Frosted overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = cat.name,
                                color = LuminaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$displayCount Wallpapers",
                                color = LuminaCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            // Category Wallpapers Detail View
            val category = selectedCategory!!
            val categoryWallpapers = remember(allWallpapers, category) {
                allWallpapers.filter { it.category.equals(category.name, ignoreCase = true) }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { selectedCategory = null },
                            contentDescription = "Back to categories"
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = category.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuminaTextPrimary
                            )
                            Text(
                                text = "${categoryWallpapers.size} curated wallpapers",
                                fontSize = 12.sp,
                                color = LuminaCyan
                            )
                        }
                    }
                }

                if (categoryWallpapers.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No wallpapers yet in this category",
                                color = LuminaTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(categoryWallpapers, key = { it.id }) { wp ->
                        WallpaperCard(
                            wallpaper = wp,
                            isFavorite = favoriteIds.contains(wp.id),
                            onClick = { onWallpaperClick(wp) },
                            onFavoriteToggle = { onFavoriteToggle(wp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
