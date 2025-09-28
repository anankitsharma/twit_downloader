package com.rit.twitdownloader.ui.component

import android.os.Build
import android.view.WindowInsetsController
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun XHeaderScaffold(
    modifier: Modifier = Modifier,
    title: String = "Home",
    content: @Composable () -> Unit = {}
) {
    // Modern topbar height following Material Design 3 guidelines
    val topBarHeight = 80.dp

    val view = LocalView.current
    SideEffect {
        // Ensure status bar icons are white for black background
        (view.context as? Activity)?.window?.let { window ->
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            windowInsetsController.isAppearanceLightStatusBars = false // false = light icons (white)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val headerBackgroundColor = Color.Black // Consistent with status bar color
        
        // Create a single Box that covers both status bar and top bar areas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
                .height(topBarHeight + statusTop)
        ) {
            // Top bar content positioned below status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .padding(horizontal = 20.dp)
                    .align(Alignment.BottomStart), // Align to bottom of the combined box
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-28).dp), // Slightly increased offset for better visual separation
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp, // Increased elevation for better depth perception
            tonalElevation = 16.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) // Consistent with AppTopBar radius
        ) {
            content()
        }
    }
}

@Composable
@Preview(name = "X Header Scaffold")
private fun XHeaderScaffoldPreview() {
    MaterialTheme {
        XHeaderScaffold() {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Content", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}



