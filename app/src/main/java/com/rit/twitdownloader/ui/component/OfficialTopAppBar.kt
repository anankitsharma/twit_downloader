package com.rit.twitdownloader.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Official Material 3 TopAppBar implementation following Android guidelines
 * 
 * Features:
 * - Follows official Material Design 3 specifications
 * - Proper edge-to-edge integration
 * - Dynamic color adaptation
 * - Accessibility compliance
 * - Multiple variants support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialTopAppBar(
    title: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    variant: TopAppBarVariant = TopAppBarVariant.Small
) {
    val containerColor = when (variant) {
        TopAppBarVariant.Small -> MaterialTheme.colorScheme.primary
        TopAppBarVariant.Medium -> MaterialTheme.colorScheme.primary
        TopAppBarVariant.Large -> MaterialTheme.colorScheme.primary
    }
    
    val titleContentColor = MaterialTheme.colorScheme.onPrimary
    val actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    val navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
    
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = titleContentColor
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleContentColor,
            actionIconContentColor = actionIconContentColor,
            navigationIconContentColor = navigationIconContentColor
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

/**
 * TopAppBar variants following Material Design 3 specifications
 */
enum class TopAppBarVariant {
    Small,    // Standard compact bar
    Medium,   // Larger bar that can collapse
    Large     // Largest variant emphasizing title
}

/**
 * Pre-configured navigation icons following Material Design 3 guidelines
 */
@Composable
fun BackNavigationIcon(
    onClick: () -> Unit,
    contentDescription: String = "Navigate back"
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun MenuNavigationIcon(
    onClick: () -> Unit,
    contentDescription: String = "Open menu"
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = contentDescription
        )
    }
}

/**
 * Common action icons following Material Design 3 guidelines
 */
@Composable
fun SearchActionIcon(
    onClick: () -> Unit,
    contentDescription: String = "Search"
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun MoreActionsIcon(
    onClick: () -> Unit,
    contentDescription: String = "More actions"
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = contentDescription
        )
    }
}
