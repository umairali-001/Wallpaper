package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaGlassBorder
import com.example.ui.theme.LuminaGlassCard
import com.example.ui.theme.LuminaGlassHighlight
import com.example.ui.theme.LuminaNeonBlue
import com.example.ui.theme.LuminaTextMuted

enum class NavTab {
    HOME, CATEGORIES, FAVORITES, PROFILE, ADMIN
}

@Composable
fun GlassBottomNavigation(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(16.dp, shape, spotColor = LuminaNeonBlue.copy(alpha = 0.25f))
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD0E1A24),
                            Color(0xF00A131C)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            LuminaGlassHighlight,
                            LuminaGlassBorder.copy(alpha = 0.4f)
                        )
                    ),
                    shape
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    icon = Icons.Outlined.Home,
                    selectedIcon = Icons.Filled.Home,
                    isSelected = selectedTab == NavTab.HOME,
                    onClick = { onTabSelected(NavTab.HOME) },
                    testTag = "nav_home"
                )

                NavItem(
                    label = "Categories",
                    icon = Icons.Outlined.Category,
                    selectedIcon = Icons.Filled.Category,
                    isSelected = selectedTab == NavTab.CATEGORIES,
                    onClick = { onTabSelected(NavTab.CATEGORIES) },
                    testTag = "nav_categories"
                )

                NavItem(
                    label = "Favorites",
                    icon = Icons.Outlined.FavoriteBorder,
                    selectedIcon = Icons.Filled.Favorite,
                    isSelected = selectedTab == NavTab.FAVORITES,
                    onClick = { onTabSelected(NavTab.FAVORITES) },
                    testTag = "nav_favorites"
                )

                NavItem(
                    label = "Profile",
                    icon = Icons.Outlined.Person,
                    selectedIcon = Icons.Filled.Person,
                    isSelected = selectedTab == NavTab.PROFILE,
                    onClick = { onTabSelected(NavTab.PROFILE) },
                    testTag = "nav_profile"
                )

                if (isAdmin) {
                    NavItem(
                        label = "Admin",
                        icon = Icons.Outlined.AdminPanelSettings,
                        selectedIcon = Icons.Filled.AdminPanelSettings,
                        isSelected = selectedTab == NavTab.ADMIN,
                        onClick = { onTabSelected(NavTab.ADMIN) },
                        testTag = "nav_admin"
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) LuminaCyan else LuminaTextMuted,
        label = "nav_icon_color"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = LuminaCyan)
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )

        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (isSelected) 14.dp else 0.dp)
                .clip(CircleShape)
                .background(if (isSelected) LuminaCyan else Color.Transparent)
        )
    }
}
