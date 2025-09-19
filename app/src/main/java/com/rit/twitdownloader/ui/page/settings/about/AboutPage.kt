﻿package com.rit.twitdownloader.ui.page.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.rit.twitdownloader.App
import com.rit.twitdownloader.App.Companion.packageInfo
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.component.BackButton
import com.rit.twitdownloader.ui.component.ConfirmButton
import com.rit.twitdownloader.ui.component.PreferenceItem
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.ToastUtil

private const val releaseURL = "https://github.com/JunkFood02/Seal/releases"
private const val repoUrl = "https://github.com/JunkFood02/Seal"
const val weblate = "https://hosted.weblate.org/engage/seal/"
const val YtdlpRepository = "https://github.com/yt-dlp/yt-dlp"
private const val githubIssueUrl = "https://github.com/JunkFood02/Seal/issues"
private const val telegramChannelUrl = "https://t.me/seal_app"
private const val matrixSpaceUrl = "https://matrix.to/#/#seal-space:matrix.org"
private const val githubSponsor = "https://github.com/sponsors/JunkFood02"
private const val TAG = "AboutPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    onNavigateToCreditsPage: () -> Unit,
    onNavigateToDonatePage: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    //    val configuration = LocalConfiguration.current
    //    val screenDensity = configuration.densityDpi / 160f
    //    val screenHeight = (configuration.screenHeightDp.toFloat() * screenDensity).roundToInt()
    //    val screenWidth = (configuration.screenWidthDp.toFloat() * screenDensity).roundToInt()

    val info = App.getVersionReport()
    val versionName = packageInfo.versionName

    //        infoBuilder.append("App version: $versionName ($versionCode)\n")
    //            .append("Device information: Android $release (API ${Build.VERSION.SDK_INT})\n")
    //            .append("Supported ABIs: ${Build.SUPPORTED_ABIS.contentToString()}\n")
    //            .append("\nScreen resolution: $screenHeight x $screenWidth")
    //            .append("Yt-dlp Version:
    // ${YoutubeDL.version(context.applicationContext)}").toString()

    val uriHandler = LocalUriHandler.current
    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.about)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = Icons.Outlined.Description,
                    ) {
                        openUrl(repoUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.release),
                        description = stringResource(R.string.release_desc),
                        icon = Icons.Outlined.NewReleases,
                    ) {
                        openUrl(releaseURL)
                    }
                }
                /*            item {
                    PreferenceItem(
                        title = stringResource(R.string.github_issue),
                        description = stringResource(R.string.github_issue_desc),
                        icon = Icons.Outlined.ContactSupport,
                    ) { openUrl(githubIssueUrl) }
                }*/
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.sponsor),
                        description = stringResource(id = R.string.sponsor_desc),
                        icon = Icons.Outlined.VolunteerActivism,
                    ) {
                        //                    openUrl(githubSponsor)
                        onNavigateToDonatePage()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.telegram_channel),
                        description = telegramChannelUrl,
                        icon = painterResource(id = R.drawable.icons8_telegram_app),
                    ) {
                        openUrl(telegramChannelUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.matrix_space),
                        description = matrixSpaceUrl,
                        icon = painterResource(id = R.drawable.icons8_matrix),
                    ) {
                        openUrl(matrixSpaceUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.credits),
                        description = stringResource(id = R.string.credits_desc),
                        icon = Icons.Outlined.AutoAwesome,
                    ) {
                        onNavigateToCreditsPage()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.version),
                        description = versionName,
                        icon = Icons.Outlined.Info,
                    ) {
                        clipboardManager.setText(AnnotatedString(info))
                        ToastUtil.makeToast(R.string.info_copied)
                    }
                }
                item {
                    PreferenceItem(title = "Package name", description = context.packageName) {
                        clipboardManager.setText(AnnotatedString(context.packageName))
                        ToastUtil.makeToast(R.string.info_copied)
                    }
                }
            }
        },
    )
}

// AutoUpdateUnavailableDialog removed - auto-update functionality removed for Play Store compliance

