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
    val topBarHeight = 72.dp

    val view = LocalView.current
    SideEffect {
        // Ensure status bar area is black and icons are light-on-dark
        (view.context as? Activity)?.window?.let { window ->
            window.statusBarColor = Color.Black.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000))
                .height(topBarHeight + statusTop)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.TopStart)
                    .padding(top = statusTop),
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
                .offset(y = (-24).dp),
            color = Color.White,
            shadowElevation = 12.dp,
            tonalElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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



