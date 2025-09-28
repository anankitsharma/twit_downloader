package com.rit.twitdownloader.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * SystemUiController following official Android guidelines
 * 
 * This implementation follows the official Android documentation for:
 * - Edge-to-edge display
 * - Dynamic color adaptation
 * - Status bar management
 * - Material Design 3 compliance
 */
@Composable
fun SystemUiController(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val view = LocalView.current
    
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        
        // Ensure compatibility across all Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Add flag to draw system bar backgrounds (required for API 21+)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            
            // Set system bar colors following official guidelines
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
        }
        
        // Configure system bar appearance based on theme
        val windowInsetsController = WindowCompat.getInsetsController(window, view)
        
        // Determine if status bar icons should be light or dark
        val useLightStatusBarIcons = if (statusBarColor == Color.Transparent) {
            // For transparent status bar, use theme-based decision
            !isDarkTheme
        } else {
            // For colored status bar, use luminance-based decision
            statusBarColor.luminance() > 0.5f
        }
        
        windowInsetsController.isAppearanceLightStatusBars = useLightStatusBarIcons
        windowInsetsController.isAppearanceLightNavigationBars = useLightStatusBarIcons
    }
}

/**
 * Material 3 compliant SystemUiController that automatically adapts to theme colors
 */
@Composable
fun Material3SystemUiController() {
    val isDarkTheme = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    SystemUiController(
        statusBarColor = Color.Black, // Black status bar to match app design
        navigationBarColor = Color.Transparent, // Always transparent for edge-to-edge
        isDarkTheme = isDarkTheme
    )
}

/**
 * Extension function to convert Compose Color to Android Color
 */
private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
