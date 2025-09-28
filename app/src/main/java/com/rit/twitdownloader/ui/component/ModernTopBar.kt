package com.rit.twitdownloader.ui.component

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.pow

/**
 * Modern TopBar component following Material Design 3 guidelines
 * Features:
 * - Proper edge-to-edge implementation
 * - Consistent status bar handling
 * - Modern spacing and typography
 * - Gradient background support
 * - Action buttons support
 */
@Composable
fun ModernTopBar(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    useGradient: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val view = LocalView.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    
    // Calculate luminance outside of SideEffect
    val isLightBackground = backgroundColor.luminance() > 0.5f
    
    // Configure status bar for modern edge-to-edge experience
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            // Keep status bar transparent to let topbar background show through
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            
            // Determine if status bar icons should be light or dark based on background
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = isLightBackground
            
            // For Android 11+ (API 30+), use the new system bars appearance API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val appearance = if (isLightBackground) {
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                } else {
                    0
                }
                view.windowInsetsController?.setSystemBarsAppearance(
                    appearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    }
    
    val backgroundBrush = if (useGradient) {
        Brush.horizontalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.95f),
                backgroundColor.copy(alpha = 0.85f)
            )
        )
    } else {
        Brush.linearGradient(listOf(backgroundColor, backgroundColor))
    }
    
    // Modern topbar with proper edge-to-edge handling
    Box(modifier = modifier.fillMaxWidth()) {
        // Status bar background that extends to the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .background(backgroundBrush)
        )
        
        // Main topbar content
        Surface(
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(backgroundBrush)
                    .padding(horizontal = 20.dp, vertical = 22.dp), // Modern spacing
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isLightBackground) {
                        Color.Black
                    } else {
                        Color.White
                    },
                    modifier = Modifier.weight(1f)
                )

                // Actions slot
                actions()
            }
        }
    }
}

/**
 * Extension function to calculate luminance of a color
 * Used to determine if status bar icons should be light or dark
 */
private fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    
    // Convert to linear RGB
    val r = if (red <= 0.03928f) red / 12.92f else ((red + 0.055f).toDouble() / 1.055).pow(2.4).toFloat()
    val g = if (green <= 0.03928f) green / 12.92f else ((green + 0.055f).toDouble() / 1.055).pow(2.4).toFloat()
    val b = if (blue <= 0.03928f) blue / 12.92f else ((blue + 0.055f).toDouble() / 1.055).pow(2.4).toFloat()
    
    // Calculate relative luminance
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
