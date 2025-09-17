package com.rit.twitdownloader.ui.page

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.rit.twitdownloader.ui.page.downloadv2.ListItemStateText
import com.rit.twitdownloader.ui.page.downloadv2.VideoListItem
import org.koin.compose.koinInject
import androidx.compose.runtime.saveable.rememberSaveable
import com.rit.twitdownloader.util.matchUrlFromSharedText

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
            val matched = matchUrlFromSharedText(clip)
            if (!matched.isNullOrEmpty() && !UrlRules.isBlocked(matched)) {
                urlText = matched
            }
            hasPastedFromClipboard = true
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
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // increased spacing for modern look
        ) {
            // Modern URL Input Card (design-only changes)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Soft label above the field
                    Text(
                        text = stringResource(R.string.enter_post_link),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Rounded filled text field
                    TextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        placeholder = { Text(stringResource(R.string.url_placeholder)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Modern Action Buttons Card (design-only changes)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                urlText = clipboardText
                                if (clipboardText.isNotEmpty() && UrlRules.isBlocked(clipboardText)) {
                                    showYouTubeWarning = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.paste),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Button(
                            onClick = {
                                if (urlText.isNotEmpty()) {
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
                                .height(48.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = urlText.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.download),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    // helper/subtext + spacing
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tip: Just paste link -> click on download — it's that simple.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // End minimal home content
            // Inline single active download card below controls
            val inlineEntry: Map.Entry<Task, Task.State>? = taskStateMap.entries.firstOrNull { it.key.url == inlineUrl }
            if (inlineEntry != null) {
                val (task, state) = inlineEntry
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        VideoListItem(
                            modifier = Modifier.fillMaxWidth(),
                            viewState = state.viewState,
                            stateIndicator = {
                                ListItemStateText(modifier = Modifier, downloadState = state.downloadState)
                            }
                        ) {
                            onOpenDownloads()
                        }
                    }
                }
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

