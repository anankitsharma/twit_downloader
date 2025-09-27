package com.rit.twitdownloader.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.rit.twitdownloader.ui.common.LocalDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BottomTab { Home, Downloads, Settings }

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        // Floating container bar
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            val barPadding = 16.dp
            Row(
                modifier = Modifier
                    .padding(horizontal = barPadding, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    selected = selectedTab == BottomTab.Home,
                    onClick = { onSelect(BottomTab.Home) }
                )

                // Spacer to accommodate the center button (56dp + some padding)
                Spacer(modifier = Modifier.size(64.dp))

                BarItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    selected = selectedTab == BottomTab.Settings,
                    onClick = { onSelect(BottomTab.Settings) }
                )
            }
        }

        // Center Downloads button overlapping the bar
        val centerSelected = selectedTab == BottomTab.Downloads
        val centerElevation: Dp by animateDpAsState(
            targetValue = if (centerSelected) 12.dp else 8.dp,
            animationSpec = tween(200, easing = FastOutSlowInEasing), label = "centerElevation"
        )
        ElevatedCard(
            shape = CircleShape,
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(centerElevation),
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(56.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button
                ) { onSelect(BottomTab.Downloads) },
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = if (centerSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Downloads",
                    tint = if (centerSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// New modern API as requested
enum class NavTab { Home, Downloads, Settings }

// Helper function to get outlined version of icons
private fun getOutlinedIcon(icon: ImageVector): ImageVector {
    return when (icon) {
        Icons.Filled.Home -> Icons.Outlined.Home
        Icons.Filled.Download -> Icons.Outlined.Download
        Icons.Filled.Settings -> Icons.Outlined.Settings
        else -> icon
    }
}

@Composable
fun ModernBottomNav(
    selectedTab: NavTab,
    onSelect: (NavTab) -> Unit,
    downloadsBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.run {
                if (LocalDarkTheme.current.isDarkTheme()) Color.Black else Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 0.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModernItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = selectedTab == NavTab.Home,
                onClick = { onSelect(NavTab.Home) }
            )

            ModernItem(
                icon = Icons.Filled.Download,
                label = "Downloads",
                selected = selectedTab == NavTab.Downloads,
                onClick = { onSelect(NavTab.Downloads) },
                badgeCount = downloadsBadgeCount
            )

            ModernItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                selected = selectedTab == NavTab.Settings,
                onClick = { onSelect(NavTab.Settings) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun ModernItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    prominent: Boolean = false,
    badgeCount: Int = 0
) {
    // Splash screen blue for selected state
    val splashBlue = Color(0xFF1DA1F2) // Splash screen blue
    val iconTint by animateColorAsState(
        targetValue = if (selected) splashBlue else Color(0xFF9E9E9E), // Blue for selected, gray for unselected
        animationSpec = tween(200), label = "iconTint"
    )
    val textTint by animateColorAsState(
        targetValue = if (selected) splashBlue else Color(0xFF9E9E9E), // Blue for selected, gray for unselected
        animationSpec = tween(200), label = "textTint"
    )

    Column(
        modifier = Modifier
            .clickable(role = Role.Tab) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (selected) icon else getOutlinedIcon(icon), 
                contentDescription = label, 
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-6).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.coerceAtMost(99).toString(),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        
        Text(
            text = label,
            color = textTint,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
@Preview(name = "ModernBottomNav Preview")
private fun ModernBottomNavPreview() {
    var selected by remember { mutableStateOf(NavTab.Home) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        ModernBottomNav(selectedTab = selected, onSelect = { selected = it })
        val bottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Spacer(modifier = Modifier.height(bottomPad))
    }
}

@Composable
private fun RowScope.BarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val pillShape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(pillShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = label, 
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
@Preview(name = "BottomNavBar Preview")
private fun BottomNavBarPreview() {
    var selected by remember { mutableStateOf(BottomTab.Home) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        BottomNavBar(selectedTab = selected, onSelect = { selected = it })
        // Simulate nav bar insets in preview
        val bottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Spacer(modifier = Modifier.height(bottomPad))
    }
}



