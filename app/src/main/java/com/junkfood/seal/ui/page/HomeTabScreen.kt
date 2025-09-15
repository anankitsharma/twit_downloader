package com.junkfood.seal.ui.page

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
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import com.junkfood.seal.R
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialog
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(
    modifier: Modifier = Modifier,
    dialogViewModel: com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel? = null,
    onMenuOpen: () -> Unit = {}
) {
    var urlText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    
    // Dialog state management (same as DownloadPageV2)
    val sheetValue by dialogViewModel?.sheetValueFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetValue.Hidden) }
    val state by dialogViewModel?.sheetStateFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.InputUrl) }
    val selectionState = dialogViewModel?.selectionStateFlow?.collectAsStateWithLifecycle()?.value ?: com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState.Idle
    
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Listen for sheet value changes (same as DownloadPageV2)
    LaunchedEffect(sheetValue) {
        if (sheetValue == com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("X Video Downloader", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onMenuOpen) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // URL Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        placeholder = { Text("Enter Post Link") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                    urlText = clipboardText
                                    if (clipboardText.isNotEmpty()) {
                                        dialogViewModel?.postAction(Action.ShowSheet(listOf(clipboardText)))
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.ContentPaste,
                                    contentDescription = "Paste from clipboard"
                                )
                            }
                        }
                    )
                }
            }

            // Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clipboardText = clipboardManager.getText()?.toString() ?: ""
                                urlText = clipboardText
                                if (clipboardText.isNotEmpty()) {
                                    dialogViewModel?.postAction(Action.ShowSheet(listOf(clipboardText)))
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (urlText.isNotEmpty()) {
                                    dialogViewModel?.postAction(Action.ShowSheet(listOf(urlText)))
                                } else {
                                    dialogViewModel?.postAction(Action.ShowSheet())
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = true // Always enabled, will use URL if available
                        ) {
                            Icon(
                                Icons.Outlined.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Download",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // End minimal home content
        }
    }
    
    // Dialog rendering (same as DownloadPageV2)
    if (showDialog && dialogViewModel != null) {
        DownloadDialog(
            state = state,
            sheetState = sheetState,
            config = com.junkfood.seal.ui.page.downloadv2.configure.Config(),
            preferences = com.junkfood.seal.util.DownloadUtil.DownloadPreferences.createFromPreferences(),
            onPreferencesUpdate = { /* preferences update */ },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }
    
    // Selection state handling (same as DownloadPageV2)
    when (selectionState) {
        is com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState.FormatSelection ->
            com.junkfood.seal.ui.page.downloadv2.configure.FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel?.postAction(Action.Reset) },
            )

        is com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            com.junkfood.seal.ui.page.downloadv2.configure.PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel?.postAction(Action.Reset) },
            )
        }

        com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState.Idle -> {}
    }
}

@Composable
@Preview(name = "Light Theme", showBackground = true)
private fun HomeTabScreenPreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null, // Preview doesn't need real ViewModel
            onMenuOpen = {}
        )
    }
}

@Composable
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun HomeTabScreenDarkPreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null, // Preview doesn't need real ViewModel
            onMenuOpen = {}
        )
    }
}

@Composable
@Preview(name = "Large Screen", device = "spec:width=1280dp,height=800dp")
private fun HomeTabScreenLargePreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null, // Preview doesn't need real ViewModel
            onMenuOpen = {}
        )
    }
}
