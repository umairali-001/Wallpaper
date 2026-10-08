package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.cloudinary.CloudinaryConfig
import com.example.data.cloudinary.CloudinaryUploader
import com.example.data.model.Category
import com.example.data.model.UserProfile
import com.example.data.model.Wallpaper
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPill
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaGold
import com.example.ui.theme.LuminaNeonBlue
import com.example.ui.theme.LuminaTeal
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaTextPrimary
import com.example.ui.theme.LuminaTextSecondary
import kotlinx.coroutines.launch

enum class AdminTab {
    OVERVIEW, WALLPAPERS, UPLOAD, CATEGORIES, USERS
}

@Composable
fun AdminDashboardScreen(
    wallpapers: List<Wallpaper>,
    categories: List<Category>,
    users: List<UserProfile>,
    onAddWallpaper: (Wallpaper) -> Unit,
    onUpdateWallpaper: (Wallpaper) -> Unit,
    onDeleteWallpaper: (String) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onToggleBlockUser: (String, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    // Upload form state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadUrlInput by remember { mutableStateOf("") }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadDescription by remember { mutableStateOf("") }
    var uploadCategory by remember { mutableStateOf("Anime") }
    var uploadTagsInput by remember { mutableStateOf("4K, OLED, Aesthetic") }
    var isFeaturedSwitch by remember { mutableStateOf(true) }
    var isTrendingSwitch by remember { mutableStateOf(true) }
    var isPopularSwitch by remember { mutableStateOf(false) }
    var isPublishedSwitch by remember { mutableStateOf(true) }
    var isUploadingToCloudinary by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060D13),
                        Color(0xFF0A151F),
                        Color(0xFF070E14)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        contentDescription = "Exit Admin"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary
                        )
                        Text(
                            text = "Cloud: ${CloudinaryConfig.cloudName} • Preset: ${CloudinaryConfig.uploadPreset}",
                            fontSize = 11.sp,
                            color = LuminaCyan
                        )
                    }
                }
            }

            // Tabs Selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AdminTab.values()) { tab ->
                    GlassPill(
                        text = when (tab) {
                            AdminTab.OVERVIEW -> "Overview"
                            AdminTab.WALLPAPERS -> "Wallpapers (${wallpapers.size})"
                            AdminTab.UPLOAD -> "Upload Wallpaper"
                            AdminTab.CATEGORIES -> "Categories"
                            AdminTab.USERS -> "Users (${users.size})"
                        },
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    AdminTab.OVERVIEW -> OverviewTabContent(wallpapers, users, categories)
                    AdminTab.WALLPAPERS -> WallpapersTabContent(
                        wallpapers = wallpapers,
                        onToggleFeatured = { wp -> onUpdateWallpaper(wp.copy(isFeatured = !wp.isFeatured)) },
                        onToggleTrending = { wp -> onUpdateWallpaper(wp.copy(isTrending = !wp.isTrending)) },
                        onTogglePopular = { wp -> onUpdateWallpaper(wp.copy(isPopular = !wp.isPopular)) },
                        onTogglePublished = { wp -> onUpdateWallpaper(wp.copy(isPublished = !wp.isPublished)) },
                        onDelete = onDeleteWallpaper
                    )
                    AdminTab.UPLOAD -> UploadTabContent(
                        selectedImageUri = selectedImageUri,
                        uploadUrlInput = uploadUrlInput,
                        uploadTitle = uploadTitle,
                        uploadDescription = uploadDescription,
                        uploadCategory = uploadCategory,
                        uploadTagsInput = uploadTagsInput,
                        isFeatured = isFeaturedSwitch,
                        isTrending = isTrendingSwitch,
                        isPopular = isPopularSwitch,
                        isPublished = isPublishedSwitch,
                        isUploading = isUploadingToCloudinary,
                        categories = categories,
                        onPickImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUrlChange = { uploadUrlInput = it },
                        onTitleChange = { uploadTitle = it },
                        onDescChange = { uploadDescription = it },
                        onCategoryChange = { uploadCategory = it },
                        onTagsChange = { uploadTagsInput = it },
                        onFeaturedChange = { isFeaturedSwitch = it },
                        onTrendingChange = { isTrendingSwitch = it },
                        onPopularChange = { isPopularSwitch = it },
                        onPublishedChange = { isPublishedSwitch = it },
                        onSubmitUpload = {
                            if (uploadTitle.isEmpty()) {
                                Toast.makeText(context, "Please enter a wallpaper title", Toast.LENGTH_SHORT).show()
                                return@UploadTabContent
                            }
                            if (selectedImageUri == null && uploadUrlInput.isEmpty()) {
                                Toast.makeText(context, "Please select an image or enter a URL", Toast.LENGTH_SHORT).show()
                                return@UploadTabContent
                            }

                            isUploadingToCloudinary = true
                            scope.launch {
                                val uploader = CloudinaryUploader()
                                val tagsList = uploadTagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                                val result = if (selectedImageUri != null) {
                                    uploader.uploadImageUri(context, selectedImageUri!!, tagsList)
                                } else {
                                    uploader.uploadFromWebUrl(uploadUrlInput, tagsList)
                                }

                                result.onSuccess { uploadResult ->
                                    val newWp = Wallpaper(
                                        title = uploadTitle,
                                        description = uploadDescription,
                                        category = uploadCategory,
                                        tags = tagsList,
                                        cloudinaryUrl = uploadResult.secureUrl,
                                        cloudinaryPublicId = uploadResult.publicId,
                                        thumbnailUrl = CloudinaryConfig.getThumbnailUrl(uploadResult.publicId),
                                        width = uploadResult.width.toLong(),
                                        height = uploadResult.height.toLong(),
                                        isFeatured = isFeaturedSwitch,
                                        isTrending = isTrendingSwitch,
                                        isPopular = isPopularSwitch,
                                        isPublished = isPublishedSwitch
                                    )
                                    onAddWallpaper(newWp)
                                    isUploadingToCloudinary = false
                                    selectedImageUri = null
                                    uploadUrlInput = ""
                                    uploadTitle = ""
                                    uploadDescription = ""
                                    Toast.makeText(context, "Wallpaper successfully published!", Toast.LENGTH_LONG).show()
                                    selectedTab = AdminTab.WALLPAPERS
                                }.onFailure { error ->
                                    isUploadingToCloudinary = false
                                    Toast.makeText(context, "Cloudinary upload failed: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                    AdminTab.CATEGORIES -> CategoriesTabContent(categories, onAddCategory, onDeleteCategory)
                    AdminTab.USERS -> UsersTabContent(users, onToggleBlockUser, onDeleteUser)
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    wallpapers: List<Wallpaper>,
    users: List<UserProfile>,
    categories: List<Category>
) {
    val totalDownloads = wallpapers.sumOf { it.downloadCount }
    val totalFavorites = wallpapers.sumOf { it.favoriteCount }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Analytics & Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LuminaTextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Total Wallpapers",
                    value = "${wallpapers.size}",
                    icon = Icons.Default.Image,
                    tint = LuminaCyan,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Users",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    tint = LuminaTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Total Downloads",
                    value = "$totalDownloads",
                    icon = Icons.Default.Download,
                    tint = LuminaGold,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Favorites",
                    value = "$totalFavorites",
                    icon = Icons.Default.Favorite,
                    tint = LuminaCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Cloudinary Connection Info
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = LuminaCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cloudinary Storage Integration",
                            fontWeight = FontWeight.Bold,
                            color = LuminaTextPrimary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AdminConfigRow("Cloud Name", CloudinaryConfig.cloudName)
                    AdminConfigRow("API Key", CloudinaryConfig.apiKey)
                    AdminConfigRow("Upload Preset", CloudinaryConfig.uploadPreset)
                    AdminConfigRow("Destination Folder", CloudinaryConfig.folder)
                    AdminConfigRow("Firestore Database", "Enterprise Custom Instance")
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = LuminaTextMuted)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuminaTextPrimary)
        }
    }
}

@Composable
private fun AdminConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LuminaTextMuted, fontSize = 12.sp)
        Text(value, color = LuminaCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WallpapersTabContent(
    wallpapers: List<Wallpaper>,
    onToggleFeatured: (Wallpaper) -> Unit,
    onToggleTrending: (Wallpaper) -> Unit,
    onTogglePopular: (Wallpaper) -> Unit,
    onTogglePublished: (Wallpaper) -> Unit,
    onDelete: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(wallpapers, search) {
        if (search.isEmpty()) wallpapers
        else wallpapers.filter { it.title.contains(search, true) || it.category.contains(search, true) }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Search Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LuminaGlassCard)
                    .border(1.dp, LuminaGlassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    textStyle = TextStyle(color = LuminaTextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(LuminaCyan),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (search.isEmpty()) Text("Search wallpapers in database...", color = LuminaTextMuted, fontSize = 13.sp)
                        inner()
                    }
                )
            }
        }

        items(filtered, key = { it.id }) { wp ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 76.dp)
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
                            Text(wp.title, fontWeight = FontWeight.Bold, color = LuminaTextPrimary, fontSize = 14.sp)
                            Text("${wp.category} • ${wp.aspectRatio}", color = LuminaCyan, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("↓ ${wp.downloadCount}  ♥ ${wp.favoriteCount}", color = LuminaTextMuted, fontSize = 11.sp)
                        }

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = LuminaCoral,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onDelete(wp.id) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LuminaGlassBorder.copy(alpha = 0.3f))

                    // Status Pill Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AdminToggleChip("Featured", wp.isFeatured, LuminaCyan) { onToggleFeatured(wp) }
                        AdminToggleChip("Trending", wp.isTrending, LuminaCoral) { onToggleTrending(wp) }
                        AdminToggleChip("Popular", wp.isPopular, LuminaGold) { onTogglePopular(wp) }
                        AdminToggleChip("Published", wp.isPublished, LuminaTeal) { onTogglePublished(wp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminToggleChip(label: String, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else LuminaGlassCard)
            .border(1.dp, if (isActive) activeColor else LuminaGlassBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else LuminaTextMuted,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun UploadTabContent(
    selectedImageUri: Uri?,
    uploadUrlInput: String,
    uploadTitle: String,
    uploadDescription: String,
    uploadCategory: String,
    uploadTagsInput: String,
    isFeatured: Boolean,
    isTrending: Boolean,
    isPopular: Boolean,
    isPublished: Boolean,
    isUploading: Boolean,
    categories: List<Category>,
    onPickImage: () -> Unit,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onFeaturedChange: (Boolean) -> Unit,
    onTrendingChange: (Boolean) -> Unit,
    onPopularChange: (Boolean) -> Unit,
    onPublishedChange: (Boolean) -> Unit,
    onSubmitUpload: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Upload Wallpaper to Cloudinary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LuminaTextPrimary
            )
        }

        // Image Selection Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .size(width = 160.dp, height = 220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, LuminaCyan, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (uploadUrlInput.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(width = 160.dp, height = 220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, LuminaCyan, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = uploadUrlInput,
                                contentDescription = "URL image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassButton(
                        text = if (selectedImageUri == null) "Pick from Gallery" else "Change Image",
                        icon = Icons.Default.Image,
                        onClick = onPickImage
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("— OR ENTER IMAGE URL —", fontSize = 11.sp, color = LuminaTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))

                    AdminTextField(value = uploadUrlInput, onValueChange = onUrlChange, placeholder = "https://example.com/wallpaper.jpg")
                }
            }
        }

        // Wallpaper Metadata Form
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Title", fontSize = 12.sp, color = LuminaTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    AdminTextField(value = uploadTitle, onValueChange = onTitleChange, placeholder = "e.g. Neon Cyber Samurai")

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Description", fontSize = 12.sp, color = LuminaTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    AdminTextField(value = uploadDescription, onValueChange = onDescChange, placeholder = "Artistic description...")

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category", fontSize = 12.sp, color = LuminaTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val categoryNames = if (categories.isNotEmpty()) categories.map { it.name }
                        else listOf("Anime", "Nature", "Cars", "Gaming", "Space", "Abstract", "Fantasy")

                        items(categoryNames) { cat ->
                            GlassPill(
                                text = cat,
                                isSelected = uploadCategory.equals(cat, true),
                                onClick = { onCategoryChange(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tags (comma-separated)", fontSize = 12.sp, color = LuminaTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    AdminTextField(value = uploadTagsInput, onValueChange = onTagsChange, placeholder = "4K, Anime, Neon, City")
                }
            }
        }

        // Switches
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdminSwitchRow("Featured on Home", isFeatured, onFeaturedChange)
                    AdminSwitchRow("Trending section", isTrending, onTrendingChange)
                    AdminSwitchRow("Popular section", isPopular, onPopularChange)
                    AdminSwitchRow("Published (Visible to users)", isPublished, onPublishedChange)
                }
            }
        }

        // Submit Button
        item {
            GlassButton(
                text = if (isUploading) "Uploading to Cloudinary..." else "Upload & Publish to App",
                icon = Icons.Default.CloudUpload,
                isLoading = isUploading,
                onClick = onSubmitUpload,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AdminSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = LuminaTextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LuminaCyan,
                checkedTrackColor = LuminaCyan.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1E2A))
            .border(1.dp, LuminaGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = LuminaTextPrimary, fontSize = 13.sp),
            cursorBrush = SolidColor(LuminaCyan),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = LuminaTextMuted, fontSize = 13.sp)
                inner()
            }
        )
    }
}

@Composable
private fun CategoriesTabContent(
    categories: List<Category>,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Category", fontWeight = FontWeight.Bold, color = LuminaTextPrimary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    AdminTextField(value = newCatName, onValueChange = { newCatName = it }, placeholder = "Category Name (e.g. Cyberpunk)")
                    Spacer(modifier = Modifier.height(8.dp))
                    AdminTextField(value = newCatIcon, onValueChange = { newCatIcon = it }, placeholder = "Icon or Preview Image URL")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Add Category",
                        icon = Icons.Default.Add,
                        onClick = {
                            if (newCatName.isNotEmpty()) {
                                onAddCategory(newCatName, newCatIcon)
                                newCatName = ""
                                newCatIcon = ""
                            }
                        }
                    )
                }
            }
        }

        items(categories, key = { it.id }) { cat ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cat.name, fontWeight = FontWeight.SemiBold, color = LuminaTextPrimary, fontSize = 14.sp)
                        Text("${cat.wallpaperCount} wallpapers", color = LuminaCyan, fontSize = 12.sp)
                    }

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = LuminaCoral,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onDeleteCategory(cat.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UsersTabContent(
    users: List<UserProfile>,
    onToggleBlock: (String, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users, key = { it.userId }) { user ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName.ifEmpty { "User" }, fontWeight = FontWeight.Bold, color = LuminaTextPrimary, fontSize = 14.sp)
                        Text(user.email, color = LuminaTextSecondary, fontSize = 12.sp)
                        Text("Role: ${user.role} • Status: ${if (user.isBlocked) "Blocked" else "Active"}", color = if (user.isBlocked) LuminaCoral else LuminaTeal, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (user.isBlocked) LuminaTeal.copy(alpha = 0.2f) else LuminaCoral.copy(alpha = 0.2f))
                                .clickable { onToggleBlock(user.userId, !user.isBlocked) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (user.isBlocked) "Unblock" else "Block",
                                color = if (user.isBlocked) LuminaTeal else LuminaCoral,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete user",
                            tint = LuminaCoral,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onDeleteUser(user.userId) }
                        )
                    }
                }
            }
        }
    }
}
