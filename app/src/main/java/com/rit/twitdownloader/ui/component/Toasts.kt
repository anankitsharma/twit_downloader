package com.rit.twitdownloader.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FloatingToast(
    message: String,
    visible: Boolean,
    autoHideMillis: Long = 1500L,
    onHide: () -> Unit,
    bottomPadding: Dp = 72.dp,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navBottom + bottomPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                tonalElevation = 0.dp,
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
    if (visible) {
        LaunchedEffect(message) {
            delay(autoHideMillis)
            onHide()
        }
    }
}

@Composable
fun BottomBanner(
    message: String,
    visible: Boolean,
    onClick: () -> Unit,
    autoHideMillis: Long = 3500L,
    onHide: () -> Unit,
    bottomPadding: Dp = 84.dp,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navBottom + bottomPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onClick() }
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
        }
    }
    if (visible) {
        LaunchedEffect(message) {
            delay(autoHideMillis)
            onHide()
        }
    }
}



