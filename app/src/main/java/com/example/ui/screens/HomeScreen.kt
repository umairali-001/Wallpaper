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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.ShimmerBox
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaGold
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaTextPrimary
import com.example.ui.theme.LuminaTextSecondary

enum class HomeFilter {
    ALL, TRENDING, POPULAR, FEATURED
}

@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String,
    wallpapers: List<Wallpaper>,
    favoriteIds: Set<String>,
    isLoading: Boolean,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Filter wallpapers according to query and filter
    val filteredWallpapers = remember(wallpapers, searchQuery, selectedFilter, selectedCategoryFilter) {
        wallpapers.filter { wp ->
            val matchesQuery = searchQuery.isEmpty() ||
                    wp.title.contains(searchQuery, ignoreCase = true) ||
                    wp.category.contains(searchQuery, ignoreCase = true) ||
                    wp.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = selectedCategoryFilter == null ||
                    wp.category.equals(selectedCategoryFilter, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                HomeFilter.ALL -> true
                HomeFilter.TRENDING -> wp.isTrending
                HomeFilter.POPULAR -> wp.isPopular
                HomeFilter.FEATURED -> wp.isFeatured
            }

            matchesQuery && matchesCategory && matchesFilter
        }
    }

    val featuredList = remember(wallpapers) {
        wallpapers.filter { it.isFeatured }
    }

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Greeting & Profile Avatar
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Good Day,",
                                fontSize = 14.sp,
                                color = LuminaTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = LuminaGold,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = userName.ifEmpty { "Lumina Explorer" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LuminaGlassCard)
                            .border(1.5.dp, LuminaCyan, CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (userPhotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = userName.take(1).uppercase().ifEmpty { "U" },
                                color = LuminaCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Glass Search Bar
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(LuminaGlassCard)
                        .border(1.dp, LuminaGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = LuminaCyan,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search wallpapers, tags, anime...",
                                    color = LuminaTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = LuminaTextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(LuminaCyan),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_input")
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = LuminaTextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }

            // Featured Wallpapers Horizontal Carousel (if search is empty)
            if (searchQuery.isEmpty() && featuredList.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Picks",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuminaTextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = LuminaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Curated",
                                    fontSize = 12.sp,
                                    color = LuminaCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(featuredList) { wp ->
                                WallpaperCard(
                                    wallpaper = wp,
                                    isFavorite = favoriteIds.contains(wp.id),
                                    onClick = { onWallpaperClick(wp) },
                                    onFavoriteToggle = { onFavoriteToggle(wp) },
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Category & Section Pills
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Explore & Discover",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuminaTextPrimary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            GlassPill(
                                text = "All Wallpapers",
                                isSelected = selectedFilter == HomeFilter.ALL && selectedCategoryFilter == null,
                                onClick = {
                                    selectedFilter = HomeFilter.ALL
                                    selectedCategoryFilter = null
                                }
                            )
                        }
                        item {
                            GlassPill(
                                text = "Trending",
                                isSelected = selectedFilter == HomeFilter.TRENDING,
                                onClick = {
                                    selectedFilter = HomeFilter.TRENDING
                                    selectedCategoryFilter = null
                                }
                            )
                        }
                        item {
                            GlassPill(
                                text = "Popular",
                                isSelected = selectedFilter == HomeFilter.POPULAR,
                                onClick = {
                                    selectedFilter = HomeFilter.POPULAR
                                    selectedCategoryFilter = null
                                }
                            )
                        }
                        items(listOf("Anime", "Nature", "Cars", "Space", "Abstract", "Gaming")) { cat ->
                            GlassPill(
                                text = cat,
                                isSelected = selectedCategoryFilter == cat,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                }
                            )
                        }
                    }
                }
            }

            // Wallpapers Grid items
            if (isLoading && wallpapers.isEmpty()) {
                items(6) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            } else if (filteredWallpapers.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No wallpapers found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuminaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search terms or filters",
                                fontSize = 13.sp,
                                color = LuminaTextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredWallpapers, key = { it.id }) { wp ->
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
