﻿package com.rit.twitdownloader.ui.page.settings.format

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.common.booleanState
import com.rit.twitdownloader.ui.common.intState
import com.rit.twitdownloader.ui.common.motion.materialSharedAxisX
import com.rit.twitdownloader.ui.common.stringState
import com.rit.twitdownloader.ui.component.ConfirmButton
import com.rit.twitdownloader.ui.component.DialogSingleChoiceItem
import com.rit.twitdownloader.ui.component.DialogSingleChoiceItemVariant
import com.rit.twitdownloader.ui.component.DialogSubtitle
import com.rit.twitdownloader.ui.component.DialogSwitchItem
import com.rit.twitdownloader.ui.component.DismissButton
import com.rit.twitdownloader.ui.component.OutlinedButtonChip
import com.rit.twitdownloader.ui.component.PreferenceSubtitle
import com.rit.twitdownloader.ui.component.SealDialog
import com.rit.twitdownloader.ui.component.SealTextField
import com.rit.twitdownloader.ui.page.downloadv2.configure.PreferencesMock
import com.rit.twitdownloader.util.AUDIO_CONVERSION_FORMAT
import com.rit.twitdownloader.util.AUDIO_CONVERT
import com.rit.twitdownloader.util.AUDIO_FORMAT
import com.rit.twitdownloader.util.AUDIO_QUALITY
import com.rit.twitdownloader.util.CONVERT_M4A
import com.rit.twitdownloader.util.CONVERT_MP3
import com.rit.twitdownloader.util.CONVERT_SUBTITLE
import com.rit.twitdownloader.util.CONVERT_VTT
import com.rit.twitdownloader.util.DEFAULT
import com.rit.twitdownloader.util.DownloadUtil
import com.rit.twitdownloader.util.FORMAT_COMPATIBILITY
import com.rit.twitdownloader.util.FORMAT_QUALITY
import com.rit.twitdownloader.util.M4A
import com.rit.twitdownloader.util.NOT_CONVERT
import com.rit.twitdownloader.util.NOT_SPECIFIED
import com.rit.twitdownloader.util.OPUS
import com.rit.twitdownloader.util.PreferenceStrings
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.PreferenceUtil.updateBoolean
import com.rit.twitdownloader.util.PreferenceUtil.updateInt
import com.rit.twitdownloader.util.PreferenceUtil.updateString
import com.rit.twitdownloader.util.RES_HIGHEST
import com.rit.twitdownloader.util.RES_LOWEST
import com.rit.twitdownloader.util.SUBTITLE_LANGUAGE
import com.rit.twitdownloader.util.ULTRA_LOW
import com.rit.twitdownloader.util.getStringDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoResolutionSelectField(
    modifier: Modifier = Modifier,
    videoResolution: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val videoResolutionText = PreferenceStrings.getVideoResolutionDesc(videoResolution)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        SealTextField(
            modifier = modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = videoResolutionText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            //            label = { Text(stringResource(id = R.string.video_resolution)) }
        )
        ExposedDropdownMenu(
            modifier = Modifier,
            scrollState = rememberScrollState(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (i in RES_HIGHEST..RES_LOWEST) DropdownMenuItem(
                text = { Text(PreferenceStrings.getVideoResolutionDesc(i)) },
                onClick = {
                    onSelect(i)
                    expanded = false
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFormatPreferenceSelectField(
    modifier: Modifier = Modifier,
    videoFormatPreference: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val videoFormatText = PreferenceStrings.getVideoFormatLabel(videoFormatPreference)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = videoFormatText,
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(Icons.Outlined.VideoFile, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            label = { Text(stringResource(id = R.string.video_format_preference)) },
        )
        ExposedDropdownMenu(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (i in listOf(FORMAT_COMPATIBILITY, FORMAT_QUALITY)) DropdownMenuItem(
                text = { Text(PreferenceStrings.getVideoFormatLabel(i)) },
                onClick = {
                    onSelect(i)
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun VideoQuickSettingsDialog(
    videoResolution: Int,
    videoFormatPreference: Int,
    onResolutionSelect: (Int) -> Unit,
    onFormatSelect: (Int) -> Unit,
    onSave: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    SealDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.VideoFile, null) },
        title = { Text(text = stringResource(id = R.string.edit_preset)) },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave()
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        text = {
            Column {
                LazyColumn() {
                    item { DialogSubtitle(text = stringResource(R.string.video_format_preference)) }
                    for (i in listOf(FORMAT_COMPATIBILITY, FORMAT_QUALITY)) {
                        item {
                            DialogSingleChoiceItemVariant(
                                modifier = Modifier,
                                title = PreferenceStrings.getVideoFormatLabel(i),
                                desc = PreferenceStrings.getVideoFormatDescComp(i),
                                selected = videoFormatPreference == i,
                            ) {
                                onFormatSelect(i)
                            }
                        }
                    }
                    item { DialogSubtitle(text = stringResource(R.string.video_resolution)) }
                    item {
                        VideoResolutionSelectField(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            videoResolution = videoResolution,
                            onSelect = onResolutionSelect,
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun VideoPreview() {
    VideoQuickSettingsDialog(
        videoResolution = RES_HIGHEST,
        videoFormatPreference = FORMAT_QUALITY,
        onResolutionSelect = {},
        onFormatSelect = {},
    ) {}
}

@Preview
@Composable
private fun AudioPreview() {
    var b by remember { mutableStateOf(false) }
    var b1 by remember { mutableStateOf(false) }

    var i1 by remember { mutableIntStateOf(1) }
    var i2 by remember { mutableIntStateOf(0) }
    var i3 by remember { mutableIntStateOf(NOT_SPECIFIED) }

    AudioQuickSettingsDialog(
        preferences = PreferencesMock,
        convertAudio = b,
        useCustomAudioPreset = b1,
        onCustomPresetToggle = { b1 = it },
        preferredFormat = i1,
        conversionFormat = i2,
        onConvertToggled = { b = it },
        onPreferredSelect = { i1 = it },
        onConversionSelect = { i2 = it },
        audioQuality = i3,
        onQualitySelect = { i3 = it },
        onSave = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioFormatSelectField(
    modifier: Modifier = Modifier,
    convertAudio: Boolean,
    preferredFormat: Int,
    conversionFormat: Int,
    onConvertToggled: (Boolean) -> Unit,
    onPreferredSelect: (Int) -> Unit,
    onConversionSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val preferredFormatText = PreferenceStrings.getAudioFormatDesc(preferredFormat)
    val conversionFormatText = PreferenceStrings.getAudioConvertDesc(conversionFormat)
    val userSelectionText = if (convertAudio) conversionFormatText else preferredFormatText

    PreferenceSubtitle(text = stringResource(R.string.audio_format))
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        SealTextField(
            modifier = modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = userSelectionText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            modifier = Modifier,
            scrollState = rememberScrollState(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (i in OPUS..M4A) {
                DropdownMenuItem(
                    text = { Text(PreferenceStrings.getAudioFormatDesc(i)) },
                    onClick = {
                        onPreferredSelect(i)
                        onConvertToggled(false)
                        expanded = false
                    },
                )
            }
            for (i in CONVERT_MP3..CONVERT_M4A) {
                DropdownMenuItem(
                    text = { Text(PreferenceStrings.getAudioConvertDesc(i)) },
                    onClick = {
                        onConversionSelect(i)
                        onConvertToggled(true)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioQualitySelectField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    audioQuality: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    PreferenceSubtitle(text = stringResource(R.string.audio_quality))
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        SealTextField(
            enabled = enabled,
            modifier =
                modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled),
            value =
                if (!enabled) stringResource(R.string.unavailable)
                else PreferenceStrings.getAudioQualityDesc(audioQuality),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            modifier = Modifier,
            scrollState = rememberScrollState(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (i in NOT_SPECIFIED..ULTRA_LOW) {
                DropdownMenuItem(
                    text = { Text(PreferenceStrings.getAudioQualityDesc(i)) },
                    onClick = {
                        onSelect(i)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun AudioQuickSettingsDialog(
    modifier: Modifier = Modifier,
    preferences: DownloadUtil.DownloadPreferences,
    onDismissRequest: () -> Unit = {},
    useCustomAudioPreset: Boolean,
    onCustomPresetToggle: (Boolean) -> Unit,
    convertAudio: Boolean,
    preferredFormat: Int,
    conversionFormat: Int,
    onConvertToggled: (Boolean) -> Unit,
    onPreferredSelect: (Int) -> Unit,
    onConversionSelect: (Int) -> Unit,
    audioQuality: Int,
    onQualitySelect: (Int) -> Unit,
    onSave: () -> Unit,
) {
    var editingPreset by remember { mutableStateOf(false) }
    SealDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.AudioFile, null) },
        title = { Text(stringResource(R.string.edit_preset)) },
        text = {
            AnimatedContent(
                editingPreset,
                transitionSpec = {
                    materialSharedAxisX(initialOffsetX = { it / 5 }, targetOffsetX = { -it / 5 })
                        .using(SizeTransform())
                },
                label = "",
            ) {
                if (!it) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        DialogSubtitle(text = stringResource(R.string.presets))
                        DialogSingleChoiceItemVariant(
                            title = stringResource(R.string.best_quality),
                            selected = !useCustomAudioPreset,
                            desc = stringResource(R.string.best_quality_desc),
                            onClick = { onCustomPresetToggle(false) },
                        )

                        DialogSingleChoiceItemVariant(
                            title = stringResource(R.string.custom),
                            selected = useCustomAudioPreset,
                            onClick = { onCustomPresetToggle(true) },
                            desc =
                                PreferenceStrings.getAudioPresetText(
                                    preferences.copy(useCustomAudioPreset = true)
                                ),
                            action = {
                                if (useCustomAudioPreset) {
                                    Spacer(Modifier.width(8.dp))
                                    VerticalDivider(Modifier.height(32.dp))
                                    IconButton(onClick = { editingPreset = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = stringResource(R.string.edit),
                                        )
                                    }
                                }
                            },
                        )
                    }
                } else {
                    Column(
                        modifier =
                            Modifier.verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                    ) {
                        AudioFormatSelectField(
                            convertAudio = convertAudio,
                            preferredFormat = preferredFormat,
                            conversionFormat = conversionFormat,
                            onConvertToggled = onConvertToggled,
                            onPreferredSelect = onPreferredSelect,
                            onConversionSelect = onConversionSelect,
                        )
                        AudioQualitySelectField(
                            audioQuality = audioQuality,
                            enabled = !convertAudio,
                            onSelect = onQualitySelect,
                        )
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
    )
}

@Composable
fun AudioConversionDialog(
    onDismissRequest: () -> Unit,
    audioFormat: Int,
    onConfirm: (Int) -> Unit = {},
) {
    var audioFormat by remember { mutableIntStateOf(audioFormat) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dismiss)) }
        },
        icon = { Icon(Icons.Outlined.Sync, null) },
        title = { Text(stringResource(R.string.convert_audio_format)) },
        confirmButton = {
            TextButton(
                onClick = {
                    AUDIO_CONVERSION_FORMAT.updateInt(audioFormat)
                    onConfirm(audioFormat)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 24.dp),
                    text = stringResource(R.string.convert_audio_format_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                for (i in CONVERT_MP3..CONVERT_M4A) DialogSingleChoiceItem(
                    modifier = Modifier,
                    text = PreferenceStrings.getAudioConvertDesc(i),
                    selected = audioFormat == i,
                ) {
                    audioFormat = i
                }
            }
        },
    )
}

@Composable
fun AudioConversionQuickSettingsDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var audioFormat by remember { mutableIntStateOf(PreferenceUtil.getAudioConvertFormat()) }
    var convertAudio by AUDIO_CONVERT.booleanState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { DismissButton { onDismissRequest() } },
        icon = { Icon(Icons.Outlined.Sync, null) },
        title = { Text(stringResource(R.string.convert_audio_format)) },
        confirmButton = {
            ConfirmButton {
                AUDIO_CONVERT.updateBoolean(convertAudio)
                AUDIO_CONVERSION_FORMAT.updateInt(audioFormat)
                onConfirm()
                onDismissRequest()
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 24.dp),
                    text = stringResource(R.string.convert_audio_format_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                DialogSingleChoiceItem(
                    text = stringResource(id = R.string.not_convert),
                    selected = !convertAudio,
                ) {
                    convertAudio = false
                }
                for (i in CONVERT_MP3..CONVERT_M4A) DialogSingleChoiceItem(
                    modifier = Modifier,
                    text = PreferenceStrings.getAudioConvertDesc(i),
                    selected = audioFormat == i && convertAudio,
                ) {
                    audioFormat = i
                    convertAudio = true
                }
            }
        },
    )
}

@Composable
@Preview
fun VideoFormatDialog(
    videoFormatPreference: Int = FORMAT_COMPATIBILITY,
    onDismissRequest: () -> Unit = {},
    onConfirm: (Int) -> Unit = {},
) {
    var preference by remember { mutableIntStateOf(videoFormatPreference) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dismiss)) }
        },
        icon = { Icon(Icons.Outlined.VideoFile, null) },
        title = { Text(stringResource(R.string.video_format_preference)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(preference)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        text = {
            Column {
                androidx.compose.material3.HorizontalDivider()
                LazyColumn(modifier = Modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
                    for (i in listOf(FORMAT_COMPATIBILITY, FORMAT_QUALITY)) item {
                        DialogSingleChoiceItemVariant(
                            modifier = Modifier,
                            title = PreferenceStrings.getVideoFormatLabel(i),
                            desc = PreferenceStrings.getVideoFormatDescComp(i),
                            selected = preference == i,
                        ) {
                            preference = i
                        }
                    }
                }
                androidx.compose.material3.HorizontalDivider()
            }
        },
    )
}

@Composable
fun AudioFormatDialog(onDismissRequest: () -> Unit) {
    var audioFormat by AUDIO_FORMAT.intState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dismiss)) }
        },
        icon = { Icon(Icons.Outlined.AudioFile, null) },
        title = { Text(stringResource(R.string.audio_format_preference)) },
        confirmButton = {
            ConfirmButton {
                AUDIO_FORMAT.updateInt(audioFormat)
                onDismissRequest()
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 24.dp),
                    text = stringResource(R.string.preferred_format_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                for (i in DEFAULT..M4A) DialogSingleChoiceItem(
                    modifier = Modifier,
                    text = PreferenceStrings.getAudioFormatDesc(i),
                    selected = audioFormat == i,
                ) {
                    audioFormat = i
                }
            }
        },
    )
}

@Composable
fun AudioQualityDialog(onDismissRequest: () -> Unit) {
    var audioQuality by AUDIO_QUALITY.intState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { DismissButton { onDismissRequest() } },
        icon = { Icon(Icons.Outlined.HighQuality, null) },
        title = { Text(stringResource(R.string.audio_quality)) },
        confirmButton = {
            ConfirmButton {
                AUDIO_QUALITY.updateInt(audioQuality)
                onDismissRequest()
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 24.dp),
                    text = stringResource(R.string.audio_quality_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                for (i in NOT_SPECIFIED..ULTRA_LOW) DialogSingleChoiceItem(
                    modifier = Modifier,
                    text = PreferenceStrings.getAudioQualityDesc(i),
                    selected = audioQuality == i,
                ) {
                    audioQuality = i
                }
            }
        },
    )
}

@Composable
fun FormatSortingDialog(
    fields: String,
    showSwitch: Boolean = false,
    toggleableValue: Boolean = false,
    onSwitchChecked: (Boolean) -> Unit = {},
    onImport: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    var sortingFields by remember(fields) { mutableStateOf(fields) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { DismissButton { onDismissRequest() } },
        icon = { Icon(Icons.AutoMirrored.Outlined.Sort, null) },
        title = { Text(stringResource(R.string.format_sorting)) },
        confirmButton = {
            ConfirmButton(text = stringResource(id = R.string.save)) {
                onConfirm(sortingFields)
                onDismissRequest()
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp),
                    text = stringResource(R.string.format_sorting_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    value = sortingFields,
                    onValueChange = { sortingFields = it },
                    leadingIcon = { Text(text = "-S", fontFamily = FontFamily.Monospace) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier =
                        Modifier.padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp)
                ) {
                    OutlinedButtonChip(
                        modifier = Modifier.padding(end = 8.dp),
                        label = stringResource(id = R.string.import_from_preferences),
                        icon = Icons.Outlined.SettingsSuggest,
                    ) {
                        onImport()
                    }
                    OutlinedButtonChip(
                        label = stringResource(R.string.yt_dlp_docs),
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    ) {
                        uriHandler.openUri(sortingFormats)
                    }
                }
                if (showSwitch) {
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    DialogSwitchItem(
                        text = stringResource(id = R.string.use_format_sorting),
                        value = toggleableValue,
                        onValueChange = onSwitchChecked,
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun FormatSortingDialogPreview() {
    var value by remember { mutableStateOf(false) }
    FormatSortingDialog(
        fields = "",
        showSwitch = true,
        toggleableValue = value,
        onSwitchChecked = { value = it },
    )
}

@Composable
fun VideoQualityDialog(
    videoQuality: Int = 0,
    onDismissRequest: () -> Unit = {},
    onConfirm: (Int) -> Unit = {},
) {
    var videoResolution by remember { mutableIntStateOf(videoQuality) }

    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dismiss)) }
        },
        icon = { Icon(Icons.Outlined.HighQuality, null) },
        title = { Text(stringResource(R.string.video_quality)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(videoResolution)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        text = {
            Column() {
                Text(
                    modifier =
                        Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 24.dp),
                    text = stringResource(R.string.video_quality_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LazyColumn() {
                    //                    item { videoResolutionSelectField() }
                    for (i in 0..7) {
                        item {
                            DialogSingleChoiceItem(
                                text = PreferenceStrings.getVideoResolutionDesc(i),
                                selected = videoResolution == i,
                            ) {
                                videoResolution = i
                            }
                        }
                    }
                }
            }
        },
    )
}

private const val subtitleOptions = "https://github.com/yt-dlp/yt-dlp#subtitle-options"
private const val sortingFormats = "https://github.com/yt-dlp/yt-dlp#sorting-formats"

@Composable
fun SubtitleLanguageDialog(onDismissRequest: () -> Unit) {
    var languages by SUBTITLE_LANGUAGE.stringState
    SubtitleLanguageDialogImpl(
        onDismissRequest = onDismissRequest,
        initialLanguages = languages,
        onReset = {
            SUBTITLE_LANGUAGE.let {
                languages = it.getStringDefault()
                it.updateString(languages)
            }
        },
        onConfirm = { SUBTITLE_LANGUAGE.updateString(it) },
    )
}

@Composable
@Preview
private fun SubtitleLanguageDialogImpl(
    onDismissRequest: () -> Unit = {},
    initialLanguages: String = "en.*,.*-orig",
    onReset: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    var languages by remember(initialLanguages) { mutableStateOf(initialLanguages) }
    val uriHandler = LocalUriHandler.current
    SealDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.subtitle_language)) },
        icon = { Icon(Icons.Outlined.Language, null) },
        text = {
            Column() {
                Text(
                    text = stringResource(id = R.string.subtitle_language_desc),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProvideTextStyle(
                    value = LocalTextStyle.current.merge(fontFamily = FontFamily.Monospace)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        value = languages,
                        onValueChange = { languages = it },
                        label = { Text(stringResource(id = R.string.subtitle_language)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier =
                        Modifier.padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp)
                ) {
                    OutlinedButtonChip(
                        modifier = Modifier.padding(end = 8.dp),
                        label = stringResource(id = R.string.reset),
                        icon = Icons.Outlined.Sync,
                    ) {
                        onReset()
                    }
                    OutlinedButtonChip(
                        label = stringResource(R.string.yt_dlp_docs),
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    ) {
                        uriHandler.openUri(sortingFormats)
                    }
                }
            }
        },
        confirmButton = {
            ConfirmButton() {
                onConfirm(languages)
                onDismissRequest()
            }
        },
        dismissButton = { DismissButton() { onDismissRequest() } },
    )
}

@Composable
fun SubtitleConversionDialog(onDismissRequest: () -> Unit) {
    var currentFormat by CONVERT_SUBTITLE.intState
    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton {
                CONVERT_SUBTITLE.updateInt(currentFormat)
                onDismissRequest()
            }
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        title = { Text(text = stringResource(id = R.string.convert_subtitle)) },
        icon = { Icon(imageVector = Icons.Outlined.Sync, contentDescription = null) },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = stringResource(id = R.string.convert_subtitle_desc),
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                for (format in NOT_CONVERT..CONVERT_VTT) {
                    item {
                        DialogSingleChoiceItem(
                            text = PreferenceStrings.getSubtitleConversionFormat(format),
                            selected = currentFormat == format,
                        ) {
                            currentFormat = format
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
fun VideoQualityPreferenceChip(
    modifier: Modifier = Modifier,
    videoQualityPreference: Int = FORMAT_COMPATIBILITY,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    ElevatedAssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(text = PreferenceStrings.getVideoFormatLabel(videoQualityPreference)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.VideoSettings,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}

@Composable
@Preview
fun VideoResolutionChip(
    modifier: Modifier = Modifier,
    videoResolution: Int = 0,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    ElevatedAssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(text = PreferenceStrings.getVideoResolutionDesc(videoResolution)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.VideoSettings,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}

