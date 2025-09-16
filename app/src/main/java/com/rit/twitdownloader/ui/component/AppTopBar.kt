package com.rit.twitdownloader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.secondary
        )
    )
    Surface(color = Color.Transparent) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(gradient)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )

            // Actions slot
            actions()
        }
    }
}



