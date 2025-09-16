package com.rit.twitdownloader.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.page.downloadv2.configure.Config
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState
import com.rit.twitdownloader.ui.page.downloadv2.configure.FormatPage
import com.rit.twitdownloader.ui.page.downloadv2.configure.InputUrlPage
import com.rit.twitdownloader.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.rit.twitdownloader.util.DownloadUtil
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadTabScreen(
    modifier: Modifier = Modifier,
    dialogViewModel: DownloadDialogViewModel = koinViewModel()
) {
    
    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val state by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(stringResource(R.string.downloads_history))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center
        ) {
            // Main download content - we'll extract this from DownloadDialog
            DownloadTabContent(
                state = state,
                config = Config(),
                preferences = preferences,
                onPreferencesUpdate = { preferences = it },
                onActionPost = { dialogViewModel.postAction(it) }
            )
        }
    }

    // Handle selection states (FormatPage, PlaylistSelectionPage)
    when (selectionState) {
        is SelectionState.FormatSelection ->
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )

        is SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        }

        SelectionState.Idle -> {}
    }
}
@Composable
private fun DownloadTabContent(
    state: SheetState,
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onActionPost: (Action) -> Unit
) {
    // This will contain the main download interface
    // We'll extract the content from DownloadDialogV2.kt
    // For now, let's create a placeholder that shows the download dialog content
    
    when (state) {
        is SheetState.Configure -> {
            // Show the configure page content directly
            ConfigurePageContent(
                state = state,
                config = config,
                preferences = preferences,
                onPreferencesUpdate = onPreferencesUpdate,
                onActionPost = onActionPost
            )
        }
        is SheetState.Error -> {
            // Show error content
            ErrorContent(state = state, onActionPost = onActionPost)
        }
        is SheetState.Loading -> {
            // Show loading content
            LoadingContent()
        }
        is SheetState.InputUrl -> {
            // Show input URL content
            InputUrlContent(
                config = config,
                onActionPost = onActionPost
            )
        }
    }
}

@Composable
private fun ConfigurePageContent(
    state: SheetState.Configure,
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onActionPost: (Action) -> Unit
) {
    // For now, show a simple configure interface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configure Download",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "URLs: ${state.urlList.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Add basic configuration options here
        Text(
            text = "Download configuration options will be implemented here",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorContent(
    state: SheetState.Error,
    onActionPost: (Action) -> Unit
) {
    // This will be extracted from DownloadDialogV2.kt
    Text(
        text = "Error: ${state.throwable.message}",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun LoadingContent() {
    Text(
        text = "Loading...",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun InputUrlContent(
    config: Config,
    onActionPost: (Action) -> Unit
) {
    InputUrlPage(
        config = config,
        onConfigUpdate = { /* Handle config update */ },
        onActionPost = onActionPost
    )
}


