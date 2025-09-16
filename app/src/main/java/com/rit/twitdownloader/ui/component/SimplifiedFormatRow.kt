package com.rit.twitdownloader.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rit.twitdownloader.util.Format
import com.rit.twitdownloader.util.toFileSizeText
import androidx.compose.material.icons.filled.PlayCircle

@Composable
fun SimplifiedFormatRow(
    modifier: Modifier = Modifier,
    thumbnailUrl: String,
    titleText: String,
    fileSizeText: String,
    showHd: Boolean,
    onDownload: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            MediaImage(
                modifier = Modifier.matchParentSize().clip(MaterialTheme.shapes.small),
                imageModel = thumbnailUrl,
                isAudio = false,
                contentDescription = null,
            )
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = fileSizeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showHd) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "HD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }

        IconButton(onClick = onDownload, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun estimateFileSizeText(format: Format, durationSeconds: Double?): String {
    val duration = durationSeconds ?: 0.0
    val sizeBytes = format.fileSize ?: format.fileSizeApprox ?: (format.tbr?.times(duration * 125))
    return sizeBytes.toFileSizeText()
}

fun computeResolutionText(format: Format): String {
    val w = format.width?.toInt()
    val h = format.height?.toInt()
    val res = when {
        w != null && h != null -> "${w}x${h}"
        !format.resolution.isNullOrBlank() -> format.resolution ?: ""
        else -> ""
    }
    val ext = (format.ext ?: "mp4").uppercase()
    return if (res.isNotBlank()) "$res / $ext" else ext
}

fun isHdFormat(format: Format): Boolean {
    val height = format.height?.toInt()
    if (height != null) return height >= 720
    val res = format.resolution ?: ""
    return res.substringAfter("x", missingDelimiterValue = "").toIntOrNull()?.let { it >= 720 } ?: false
}



