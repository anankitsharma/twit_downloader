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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun ModernBottomNav(
    selectedTab: NavTab,
    onSelect: (NavTab) -> Unit,
    downloadsBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModernItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = selectedTab == NavTab.Home,
                onClick = { onSelect(NavTab.Home) }
            )

            // Center Downloads - slightly raised and prominent
            val raise by animateDpAsState(
                targetValue = if (selectedTab == NavTab.Downloads) 8.dp else 6.dp,
                animationSpec = tween(200, easing = FastOutSlowInEasing), label = "raise"
            )
            Box(modifier = Modifier.offset(y = -raise)) {
                ModernItem(
                    icon = Icons.Filled.Download,
                    label = "Downloads",
                    selected = selectedTab == NavTab.Downloads,
                    onClick = { onSelect(NavTab.Downloads) },
                    prominent = true,
                    badgeCount = downloadsBadgeCount
                )
            }

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
    val pillHeight = 44.dp
    val pillShape = RoundedCornerShape(20.dp)
    val targetBg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent
    val bg by animateColorAsState(targetValue = targetBg, animationSpec = tween(200), label = "bg")
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200), label = "tint"
    )
    val scale by animateFloatAsState(targetValue = if (selected && prominent) 1.06f else 1f, animationSpec = tween(200), label = "scale")

    Row(
        modifier = Modifier
            .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
            .heightIn(min = 48.dp)
            .clip(pillShape)
            .background(bg)
            .clickable(role = Role.Tab) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon, 
                contentDescription = label, 
                tint = tint,
                modifier = Modifier.size(20.dp)
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
        AnimatedContent(targetState = selected, transitionSpec = {
            fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(150))
        }, label = "pillLabel") { show ->
            if (show) {
                Text(
                    text = label,
                    color = tint,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
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



