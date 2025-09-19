﻿@file:OptIn(ExperimentalPermissionsApi::class)

package com.rit.twitdownloader.ui.page.settings.directory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.SnippetFolder
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.TabUnselected
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.rit.twitdownloader.App
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.common.booleanState
import com.rit.twitdownloader.ui.common.stringState
import com.rit.twitdownloader.ui.component.BackButton
import com.rit.twitdownloader.ui.component.ConfirmButton
import com.rit.twitdownloader.ui.component.DialogSingleChoiceItem
import com.rit.twitdownloader.ui.component.DismissButton
import com.rit.twitdownloader.ui.component.LinkButton
import com.rit.twitdownloader.ui.component.OutlinedButtonChip
import com.rit.twitdownloader.ui.component.PreferenceInfo
import com.rit.twitdownloader.ui.component.PreferenceItem
import com.rit.twitdownloader.ui.component.PreferenceSubtitle
import com.rit.twitdownloader.ui.component.PreferenceSwitch
import com.rit.twitdownloader.ui.component.PreferenceSwitchWithDivider
import com.rit.twitdownloader.ui.component.PreferencesHintCard
import com.rit.twitdownloader.ui.component.SealDialog
import com.rit.twitdownloader.util.COMMAND_DIRECTORY
import com.rit.twitdownloader.util.CUSTOM_COMMAND
import com.rit.twitdownloader.util.CUSTOM_OUTPUT_TEMPLATE
import com.rit.twitdownloader.util.DownloadUtil
import com.rit.twitdownloader.util.FileUtil
import com.rit.twitdownloader.util.FileUtil.getConfigDirectory
import com.rit.twitdownloader.util.FileUtil.getExternalTempDir
import com.rit.twitdownloader.util.OUTPUT_TEMPLATE
import com.rit.twitdownloader.util.PRIVATE_DIRECTORY
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.PreferenceUtil.getBoolean
import com.rit.twitdownloader.util.PreferenceUtil.getString
import com.rit.twitdownloader.util.PreferenceUtil.updateBoolean
import com.rit.twitdownloader.util.PreferenceUtil.updateString
import com.rit.twitdownloader.util.RESTRICT_FILENAMES
import com.rit.twitdownloader.util.SDCARD_DOWNLOAD
import com.rit.twitdownloader.util.SDCARD_URI
import com.rit.twitdownloader.util.SUBDIRECTORY_EXTRACTOR
import com.rit.twitdownloader.util.SUBDIRECTORY_PLAYLIST_TITLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ytdlpOutputTemplateReference = "https://github.com/yt-dlp/yt-dlp#output-template"
private val PublicDownloadsDirectory =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
private val PublicDocumentDirectory =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path
private const val ytdlpFilesystemReference = "https://github.com/yt-dlp/yt-dlp#filesystem-options"

private fun String.isValidDirectory(): Boolean {
    return isEmpty() || contains(PublicDownloadsDirectory) || contains(PublicDocumentDirectory)
}

enum class Directory {
    AUDIO,
    VIDEO,
    SDCARD,
    CUSTOM_COMMAND,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DownloadDirectoryPreferences(onNavigateBack: () -> Unit) {

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showSubdirectoryDialog by remember { mutableStateOf(false) }

    var isPrivateDirectoryEnabled by remember { mutableStateOf(PRIVATE_DIRECTORY.getBoolean()) }

    var videoDirectoryText by
        remember(isPrivateDirectoryEnabled) {
            mutableStateOf(
                if (!isPrivateDirectoryEnabled) App.videoDownloadDir else App.privateDownloadDir
            )
        }
    var audioDirectoryText by
        remember(isPrivateDirectoryEnabled) {
            mutableStateOf(
                if (!isPrivateDirectoryEnabled) App.audioDownloadDir else App.privateDownloadDir
            )
        }
    var sdcardUri by remember { mutableStateOf(SDCARD_URI.getString()) }
    var customCommandDirectory by COMMAND_DIRECTORY.stringState

    var sdcardDownload by remember { mutableStateOf(SDCARD_DOWNLOAD.getBoolean()) }

    var showClearTempDialog by remember { mutableStateOf(false) }
    var showCustomCommandDirectoryDialog by remember { mutableStateOf(false) }

    var editingDirectory by remember { mutableStateOf(Directory.VIDEO) }

    val isCustomCommandEnabled by remember { mutableStateOf(CUSTOM_COMMAND.getBoolean()) }

    var showOutputTemplateDialog by remember { mutableStateOf(false) }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    // Removed showDirectoryAlert - no longer using MANAGE_EXTERNAL_STORAGE permission

    val launcher =
        rememberLauncherForActivityResult(
            object : ActivityResultContracts.OpenDocumentTree() {
                override fun createIntent(context: Context, input: Uri?): Intent {
                    return (super.createIntent(context, input)).apply {
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    }
                }
            }
        ) {
            it?.let { uri ->
                App.updateDownloadDir(uri, editingDirectory)
                if (editingDirectory != Directory.SDCARD) {
                    val path = FileUtil.getRealPath(uri)
                    when (editingDirectory) {
                        Directory.AUDIO -> {
                            audioDirectoryText = path
                        }

                        Directory.VIDEO -> {
                            videoDirectoryText = path
                        }

                        Directory.SDCARD -> {
                            sdcardUri = uri.toString()
                        }

                        Directory.CUSTOM_COMMAND -> {
                            customCommandDirectory = path
                        }
                    }
                }
            }
        }

    fun openDirectoryChooser(directory: Directory = Directory.VIDEO) {
        editingDirectory = directory
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted)
            launcher.launch(null)
        else storagePermission.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(modifier = Modifier.systemBarsPadding(), hostState = snackbarHostState)
        },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.download_directory),
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        LazyColumn(modifier = Modifier, contentPadding = it) {
            if (isCustomCommandEnabled)
                item {
                    PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
                }

            item { PreferenceSubtitle(text = stringResource(R.string.general_settings)) }
            if (!isCustomCommandEnabled) {
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_directory),
                        description = videoDirectoryText,
                        enabled = !isPrivateDirectoryEnabled && !sdcardDownload,
                        icon = Icons.Outlined.VideoLibrary,
                    ) {
                        openDirectoryChooser(directory = Directory.VIDEO)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_directory),
                        description = audioDirectoryText,
                        enabled = !isPrivateDirectoryEnabled && !sdcardDownload,
                        icon = Icons.Outlined.LibraryMusic,
                    ) {
                        openDirectoryChooser(directory = Directory.AUDIO)
                    }
                }
            }
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.custom_command_directory),
                    description =
                        customCommandDirectory.ifEmpty {
                            stringResource(id = R.string.set_directory_desc)
                        },
                    icon = Icons.Outlined.Folder,
                ) {
                    showCustomCommandDirectoryDialog = true
                }
            }
            item {
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.sdcard_directory),
                    description =
                        sdcardUri.ifEmpty { stringResource(id = R.string.set_directory_desc) },
                    isChecked = sdcardDownload,
                    enabled = !isCustomCommandEnabled,
                    isSwitchEnabled = !isCustomCommandEnabled,
                    onChecked = {
                        if (sdcardUri.isNotEmpty()) {
                            sdcardDownload = !sdcardDownload
                            PreferenceUtil.updateValue(SDCARD_DOWNLOAD, sdcardDownload)
                        } else {
                            openDirectoryChooser(Directory.SDCARD)
                        }
                    },
                    icon = Icons.Outlined.SdCard,
                    onClick = { openDirectoryChooser(Directory.SDCARD) },
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.subdirectory),
                    description = stringResource(id = R.string.subdirectory_desc),
                    icon = Icons.Outlined.SnippetFolder,
                    enabled = !isCustomCommandEnabled && !sdcardDownload,
                ) {
                    showSubdirectoryDialog = true
                }
            }
            item { PreferenceSubtitle(text = stringResource(R.string.privacy)) }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.private_directory),
                    description = stringResource(R.string.private_directory_desc),
                    icon = Icons.Outlined.TabUnselected,
                    enabled = !sdcardDownload && !isCustomCommandEnabled,
                    isChecked = isPrivateDirectoryEnabled,
                    onClick = {
                        isPrivateDirectoryEnabled = !isPrivateDirectoryEnabled
                        PreferenceUtil.updateValue(PRIVATE_DIRECTORY, isPrivateDirectoryEnabled)
                    },
                )
            }
            item { PreferenceSubtitle(text = stringResource(R.string.advanced_settings)) }
            item {
                PreferenceItem(
                    title = stringResource(R.string.output_template),
                    description = stringResource(id = R.string.output_template_desc),
                    icon = Icons.Outlined.FolderSpecial,
                    enabled = !isCustomCommandEnabled && !sdcardDownload,
                    onClick = { showOutputTemplateDialog = true },
                )
            }
            item {
                var restrictFilenames by RESTRICT_FILENAMES.booleanState
                PreferenceSwitch(
                    title = stringResource(id = R.string.restrict_filenames),
                    icon = Icons.Outlined.Spellcheck,
                    description = stringResource(id = R.string.restrict_filenames_desc),
                    isChecked = restrictFilenames,
                ) {
                    restrictFilenames = !restrictFilenames
                    RESTRICT_FILENAMES.updateBoolean(restrictFilenames)
                }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.clear_temp_files),
                    description = stringResource(R.string.clear_temp_files_desc),
                    icon = Icons.Outlined.FolderDelete,
                    onClick = { showClearTempDialog = true },
                )
            }
        }
    }

    if (showClearTempDialog) {
        AlertDialog(
            onDismissRequest = { showClearTempDialog = false },
            icon = { Icon(Icons.Outlined.FolderDelete, null) },
            title = { Text(stringResource(id = R.string.clear_temp_files)) },
            dismissButton = { DismissButton { showClearTempDialog = false } },
            text = {
                Text(
                    stringResource(
                        R.string.clear_temp_files_info,
                        getExternalTempDir().absolutePath,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                ConfirmButton {
                    showClearTempDialog = false
                    scope.launch(Dispatchers.IO) {
                        FileUtil.clearTempFiles(context.getConfigDirectory())
                        val count =
                            FileUtil.run {
                                clearTempFiles(getExternalTempDir()) +
                                    clearTempFiles(context.getSdcardTempDir(null)) +
                                    clearTempFiles(context.getInternalTempDir())
                            }

                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.clear_temp_files_count).format(count)
                            )
                        }
                    }
                }
            },
        )
    }
    val outputTemplate by
        remember(showOutputTemplateDialog) { mutableStateOf(OUTPUT_TEMPLATE.getString()) }
    val customTemplate by
        remember(showOutputTemplateDialog) { mutableStateOf(CUSTOM_OUTPUT_TEMPLATE.getString()) }
    if (showOutputTemplateDialog) {
        OutputTemplateDialog(
            selectedTemplate = outputTemplate,
            customTemplate = customTemplate,
            onDismissRequest = { showOutputTemplateDialog = false },
            onConfirm = { selected, custom ->
                OUTPUT_TEMPLATE.updateString(selected)
                CUSTOM_OUTPUT_TEMPLATE.updateString(custom)
                showOutputTemplateDialog = false
            },
        )
    }
    if (showCustomCommandDirectoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCommandDirectoryDialog = false },
            icon = { Icon(imageVector = Icons.Outlined.Folder, contentDescription = null) },
            title = {
                Text(
                    text = stringResource(id = R.string.custom_command_directory),
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                ConfirmButton {
                    COMMAND_DIRECTORY.updateString(customCommandDirectory)
                    showCustomCommandDirectoryDialog = false
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        text = stringResource(R.string.custom_command_directory_desc),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    OutlinedTextField(
                        modifier = Modifier.padding(vertical = 8.dp),
                        value = customCommandDirectory,
                        onValueChange = { customCommandDirectory = it },
                        leadingIcon = { Text(text = "-P", fontFamily = FontFamily.Monospace) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        OutlinedButtonChip(
                            modifier = Modifier.padding(end = 8.dp),
                            label = stringResource(id = R.string.folder_picker),
                            icon = Icons.Outlined.FolderOpen,
                        ) {
                            openDirectoryChooser(Directory.CUSTOM_COMMAND)
                        }
                        OutlinedButtonChip(
                            label = stringResource(R.string.yt_dlp_docs),
                            icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        ) {
                            uriHandler.openUri(ytdlpFilesystemReference)
                        }
                    }
                }
            },
            dismissButton = { DismissButton { showCustomCommandDirectoryDialog = false } },
        )
    }
    if (showSubdirectoryDialog) {
        DirectoryPreferenceDialog(
            onDismissRequest = { showSubdirectoryDialog = false },
            isWebsiteSelected = SUBDIRECTORY_EXTRACTOR.getBoolean(),
            isPlaylistTitleSelected = SUBDIRECTORY_PLAYLIST_TITLE.getBoolean(),
            onConfirm = { isWebsiteSelected, isPlaylistTitleSelected ->
                SUBDIRECTORY_EXTRACTOR.updateBoolean(isWebsiteSelected)
                SUBDIRECTORY_PLAYLIST_TITLE.updateBoolean(isPlaylistTitleSelected)
            },
        )
    }
}

@Composable
@Preview
fun OutputTemplateDialog(
    selectedTemplate: String = DownloadUtil.OUTPUT_TEMPLATE_DEFAULT,
    customTemplate: String = DownloadUtil.OUTPUT_TEMPLATE_ID,
    onDismissRequest: () -> Unit = {},
    onConfirm: (String, String) -> Unit = { s, s1 -> },
) {
    var editingTemplate by remember { mutableStateOf(customTemplate) }

    var selectedItem by remember {
        mutableIntStateOf(
            when (selectedTemplate) {
                DownloadUtil.OUTPUT_TEMPLATE_DEFAULT -> 1
                DownloadUtil.OUTPUT_TEMPLATE_ID -> 2
                else -> 3
            }
        )
    }

    var error by remember { mutableIntStateOf(0) }

    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton(enabled = error == 0) {
                onConfirm(
                    when (selectedItem) {
                        1 -> DownloadUtil.OUTPUT_TEMPLATE_DEFAULT
                        2 -> DownloadUtil.OUTPUT_TEMPLATE_ID
                        else -> editingTemplate
                    },
                    editingTemplate,
                )
            }
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        title = { Text(text = stringResource(id = R.string.output_template)) },
        icon = { Icon(imageVector = Icons.Outlined.FolderSpecial, contentDescription = null) },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.output_template_desc),
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CompositionLocalProvider(
                        LocalTextStyle provides
                            LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    ) {
                        DialogSingleChoiceItem(
                            text = DownloadUtil.OUTPUT_TEMPLATE_DEFAULT,
                            selected = selectedItem == 1,
                        ) {
                            selectedItem = 1
                        }
                        DialogSingleChoiceItem(
                            text = DownloadUtil.OUTPUT_TEMPLATE_ID,
                            selected = selectedItem == 2,
                        ) {
                            selectedItem = 2
                        }
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            RadioButton(
                                modifier = Modifier.clearAndSetSemantics {},
                                selected = selectedItem == 3,
                                onClick = { selectedItem = 3 },
                            )
                            OutlinedTextField(
                                value = editingTemplate,
                                onValueChange = {
                                    error =
                                        if (!it.contains(DownloadUtil.BASENAME)) {
                                            1
                                        } else if (!it.endsWith(DownloadUtil.EXTENSION)) {
                                            2
                                        } else {
                                            0
                                        }
                                    editingTemplate = it
                                },
                                isError = error != 0,
                                supportingText = {
                                    Text(
                                        "Required: ${DownloadUtil.BASENAME}, ${DownloadUtil.EXTENSION}",
                                        fontFamily = FontFamily.Monospace,
                                    )
                                },
                                label = { Text(text = stringResource(id = R.string.custom)) },
                            )
                        }
                    }
                }

                LinkButton(
                    link = ytdlpOutputTemplateReference,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        },
    )
}

