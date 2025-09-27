package com.rit.twitdownloader.ui.page

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.component.XHeaderScaffold
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.rit.twitdownloader.ui.util.UrlRules
import androidx.compose.ui.graphics.Color
import com.rit.twitdownloader.util.DownloadUtil
import com.rit.twitdownloader.download.DownloaderV2
import com.rit.twitdownloader.download.Task
import com.rit.twitdownloader.ui.common.AsyncImageImpl
import com.rit.twitdownloader.ui.page.downloadv2.ListItemStateText
import com.rit.twitdownloader.ui.page.downloadv2.VideoListItem
import org.koin.compose.koinInject
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.derivedStateOf
import com.rit.twitdownloader.util.findURLsFromString
import com.rit.twitdownloader.util.SharedUrlBus
import com.rit.twitdownloader.util.getUrlValidationErrorMessage
import com.rit.twitdownloader.util.looksLikeUrl
import com.rit.twitdownloader.util.ToastUtil
import com.rit.twitdownloader.util.toDurationText
import com.rit.twitdownloader.util.toFileSizeText
import com.rit.twitdownloader.util.FileUtil
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(
    modifier: Modifier = Modifier,
    dialogViewModel: DownloadDialogViewModel? = null,
    onOpenDownloads: () -> Unit = {}
) {
    var urlText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val downloader: DownloaderV2 = koinInject()
    val taskStateMap = downloader.getTaskStateMap()
    var inlineUrl by rememberSaveable { mutableStateOf("") }
    var hasPastedFromClipboard by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    // Find the most recent download to show on homepage - includes completed downloads for persistence
    val activeDownload by remember {
        derivedStateOf {
            val allTasks = taskStateMap.entries.toList()
            val relevantTasks = allTasks.filter { (_, state) -> 
                state.downloadState is Task.DownloadState.Running ||
                state.downloadState is Task.DownloadState.FetchingInfo ||
                state.downloadState is Task.DownloadState.ReadyWithInfo ||
                state.downloadState is Task.DownloadState.Idle ||
                state.downloadState is Task.DownloadState.Completed
            }
            val result = relevantTasks.maxByOrNull { it.key.timeCreated }
            
            // Debug logging
            android.util.Log.d("HomeTabScreen", "TaskStateMap size: ${allTasks.size}, Relevant tasks: ${relevantTasks.size}")
            allTasks.forEach { (task, state) ->
                android.util.Log.d("HomeTabScreen", "Task: ${task.url}, State: ${state.downloadState}")
            }
            if (result != null) {
                android.util.Log.d("HomeTabScreen", "Selected active download: ${result.key.url}, State: ${result.value.downloadState}")
            }
            
            result
        }
    }

    // Dialog state management (same as DownloadPageV2)
    val sheetValue by dialogViewModel?.sheetValueFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(DownloadDialogViewModel.SheetValue.Hidden) }
    val state by dialogViewModel?.sheetStateFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(DownloadDialogViewModel.SheetState.InputUrl) }
    val selectionState =
        dialogViewModel?.selectionStateFlow?.collectAsStateWithLifecycle()?.value
            ?: SelectionState.Idle

    var showDialog by remember { mutableStateOf(false) }
    var showYouTubeWarning by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // One-time auto-paste from clipboard on first open if the clipboard contains a URL
    LaunchedEffect(Unit) {
        if (!hasPastedFromClipboard && urlText.isEmpty()) {
            val clip = clipboardManager.getText()?.toString() ?: ""
            // Only try to match URLs if clipboard actually has content
            if (clip.isNotEmpty()) {
                // Silently extract first URL from clipboard without showing any toast
                val matched = findURLsFromString(clip, true).joinToString(separator = "\n")
                if (matched.isNotEmpty() && !UrlRules.isBlocked(matched)) {
                    urlText = matched
                }
            }
            hasPastedFromClipboard = true
        }
    }

    // Listen for shared URLs from the system share sheet and paste into input
    LaunchedEffect(Unit) {
        SharedUrlBus.urls.collectLatest { incomingUrl ->
            if (incomingUrl.isNotEmpty() && !UrlRules.isBlocked(incomingUrl)) {
                urlText = incomingUrl
                focusRequester.requestFocus()
            }
        }
    }

    // Listen for sheet value changes (same as DownloadPageV2)
    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            // hide is suspendable - call directly in this coroutine scope, then update flag
            sheetState.hide()
            showDialog = false
        }
    }

    XHeaderScaffold(title = stringResource(R.string.x_video_downloader)) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Floating card near top - full width for mobile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // URL Input Field with Twitter/X styling
                    TextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        placeholder = { 
                            Text(
                                text = "https://...",
                                color = Color(0xFF657786) // Twitter light gray
                            ) 
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF1A1A1A)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                urlText = clipboardText
                                if (clipboardText.isNotEmpty() && UrlRules.isBlocked(clipboardText)) {
                                    showYouTubeWarning = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = stringResource(R.string.paste_from_clipboard),
                                    tint = Color(0xFF1DA1F2), // Twitter blue
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F9FA), // Light gray background
                            unfocusedContainerColor = Color(0xFFF7F9FA),
                            disabledContainerColor = Color(0xFFF7F9FA),
                            cursorColor = Color(0xFF1DA1F2), // Twitter blue cursor
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedPlaceholderColor = Color(0xFF657786),
                            unfocusedPlaceholderColor = Color(0xFF657786)
                        )
                    )

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Paste Button - Twitter blue
                        Button(
                            onClick = {
                                val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                urlText = clipboardText
                                if (clipboardText.isNotEmpty() && UrlRules.isBlocked(clipboardText)) {
                                    showYouTubeWarning = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DA1F2), // Twitter blue
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.paste),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Download Button - Twitter blue
                        Button(
                            onClick = {
                                if (urlText.isNotEmpty()) {
                                    // Enhanced URL validation
                                    if (!looksLikeUrl(urlText)) {
                                        ToastUtil.makeToast(getUrlValidationErrorMessage(urlText))
                                        return@Button
                                    }
                                    
                                    if (UrlRules.isBlocked(urlText)) {
                                        showYouTubeWarning = true
                                    } else {
                                        // Direct preset download: reuse same action as preset buttons
                                        val prefs = DownloadUtil.DownloadPreferences.createFromPreferences()
                                        dialogViewModel?.postAction(
                                            Action.DownloadWithPreset(urlList = listOf(urlText), preferences = prefs)
                                        )
                                        inlineUrl = urlText
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DA1F2), // Twitter blue
                                contentColor = Color.White
                            ),
                            enabled = urlText.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.download),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tip text
                    Text(
                        text = "Tip: Just paste the link → click download. It's that simple.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF657786), // Twitter light gray
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Show active download card below the main card - persists across tab switches and app restarts
            activeDownload?.let { (task, state) ->
                // Debug logging
                LaunchedEffect(task.id, state.downloadState) {
                    android.util.Log.d("HomeTabScreen", "Showing download card for task: ${task.url}, state: ${state.downloadState}")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ModernDownloadCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    task = task,
                    state = state,
                    onPlayVideo = { filePath ->
                        FileUtil.openFile(path = filePath) { 
                            ToastUtil.makeToastSuspend("File not available")
                        }
                    },
                    onOpenDownloads = onOpenDownloads
                )
            }
        }
    }

    if (showYouTubeWarning) {
        AlertDialog(
            onDismissRequest = { showYouTubeWarning = false },
            title = { Text(stringResource(R.string.warning)) },
            text = { Text(stringResource(R.string.youtube_block_msg)) },
            confirmButton = { TextButton(onClick = { showYouTubeWarning = false }) { Text(text = stringResource(id = android.R.string.ok)) } }
        )
    }

    // Remove dialog and selection UI on Home to bypass configuration entirely
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDownloadCard(
    modifier: Modifier = Modifier,
    task: Task,
    state: Task.State,
    onPlayVideo: (String) -> Unit,
    onOpenDownloads: () -> Unit
) {
    val downloadState = state.downloadState
    val viewState = state.viewState
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                when (downloadState) {
                    is Task.DownloadState.Completed -> {
                        downloadState.filePath?.let { onPlayVideo(it) }
                    }
                    else -> onOpenDownloads()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            // Thumbnail with overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                // Thumbnail image
                AsyncImageImpl(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large),
                    model = viewState.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                
                // Dark overlay for better text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.3f)
                        )
                )
                
                // Centered progress indicator or play button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (downloadState) {
                        is Task.DownloadState.Completed -> {
                            // Play button for completed downloads
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play video",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        is Task.DownloadState.Running -> {
                            // Progress indicator for running downloads
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Always show progress indicator
                                if (downloadState.progress >= 0f) {
                                    CircularProgressIndicator(
                                        progress = downloadState.progress,
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 4.dp
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 4.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Show progress text
                                val progressText = when {
                                    downloadState.progress >= 0f -> "${(downloadState.progress * 100).toInt()}%"
                                    else -> "Starting..."
                                }
                                
                                Text(
                                    text = progressText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Debug logging
                                LaunchedEffect(downloadState.progress, downloadState.progressText) {
                                    android.util.Log.d("HomeTabScreen", "Running progress: ${downloadState.progress}, progressText: '${downloadState.progressText}', displayText: '$progressText'")
                                }
                            }
                        }
                        is Task.DownloadState.FetchingInfo -> {
                            // Indeterminate progress for fetching info
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Fetching info...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        else -> {
                            // Default state
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Preparing...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Status indicator in top-right corner
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = when (downloadState) {
                        is Task.DownloadState.Completed -> MaterialTheme.colorScheme.primary
                        is Task.DownloadState.Running -> MaterialTheme.colorScheme.secondary
                        is Task.DownloadState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (downloadState) {
                                is Task.DownloadState.Completed -> Icons.Filled.CheckCircle
                                is Task.DownloadState.Running -> Icons.Outlined.Download
                                is Task.DownloadState.Error -> Icons.Outlined.Error
                                else -> Icons.Outlined.Download
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (downloadState) {
                                is Task.DownloadState.Completed -> MaterialTheme.colorScheme.onPrimary
                                is Task.DownloadState.Running -> MaterialTheme.colorScheme.onSecondary
                                is Task.DownloadState.Error -> MaterialTheme.colorScheme.onError
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = when (downloadState) {
                                is Task.DownloadState.Completed -> "Downloaded"
                                is Task.DownloadState.Running -> "Downloading"
                                is Task.DownloadState.FetchingInfo -> "Fetching"
                                is Task.DownloadState.Error -> "Error"
                                else -> "Preparing"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (downloadState) {
                                is Task.DownloadState.Completed -> MaterialTheme.colorScheme.onPrimary
                                is Task.DownloadState.Running -> MaterialTheme.colorScheme.onSecondary
                                is Task.DownloadState.Error -> MaterialTheme.colorScheme.onError
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = viewState.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Uploader
                if (viewState.uploader.isNotEmpty()) {
                    Text(
                        text = viewState.uploader,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // File info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration and file size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewState.duration > 0) {
                            Text(
                                text = viewState.duration.toDurationText(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (viewState.fileSizeApprox > 0) {
                            Text(
                                text = viewState.fileSizeApprox.toFileSizeText(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Action hint
                    Text(
                        text = when (downloadState) {
                            is Task.DownloadState.Completed -> "Tap to play"
                            else -> "Tap for details"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
@Preview(name = "Light Theme", showBackground = true)
private fun HomeTabScreenPreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null // Preview doesn't need real ViewModel
        )
    }
}

@Composable
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun HomeTabScreenDarkPreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null // Preview doesn't need real ViewModel
        )
    }
}

@Composable
@Preview(name = "Large Screen", device = "spec:width=1280dp,height=800dp")
private fun HomeTabScreenLargePreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null // Preview doesn't need real ViewModel
        )
    }
}

