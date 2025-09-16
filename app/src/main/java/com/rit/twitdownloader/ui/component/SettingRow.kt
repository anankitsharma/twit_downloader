package com.rit.twitdownloader.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    badgeColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    // Outer spacing so stacked cards have breathing room
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // << outer spacing
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // slightly higher for float effect
        shape = RoundedCornerShape(12.dp) // softer corners
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp), // << more internal padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon badge — slightly larger for visual balance
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = badgeColor,
                shape = CircleShape,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            // optional affordance (chevron) — uncomment if desired:
            // Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

