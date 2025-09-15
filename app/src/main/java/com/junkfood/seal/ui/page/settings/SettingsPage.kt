package com.junkfood.seal.ui.page.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material.icons.rounded.SignalCellular4Bar
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material.icons.rounded.ViewComfy
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.SettingRow
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.util.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.DarkThemePreference.Companion.OFF
import com.junkfood.seal.util.DarkThemePreference.Companion.ON
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_SPONSOR_MSG

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    // Minimal settings: remove battery/sponsor flows, keep simple items only

    val darkThemePreference = LocalDarkTheme.current
    var showThemeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
            // Dark Theme (top-level)
            item {
                val themeSubtitle = when (darkThemePreference.darkThemeValue) {
                    FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
                    ON -> stringResource(R.string.on)
                    OFF -> stringResource(R.string.off)
                    else -> stringResource(R.string.follow_system)
                }
                SettingRow(
                    icon = Icons.Rounded.Palette,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    title = stringResource(id = R.string.dark_theme),
                    subtitle = themeSubtitle,
                    onClick = { showThemeDialog = true }
                )
            }
            // Display Language (top-level)
            item {
                val langSubtitle = "English" // Default fallback
                SettingRow(
                    icon = Icons.Outlined.Language,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    badgeColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    title = stringResource(id = R.string.language),
                    subtitle = langSubtitle,
                    onClick = { onNavigateTo(Route.LANGUAGES) }
                )
            }
            // Display settings (optional link to full appearance page if needed)
            item {
                SettingRow(
                    icon = Icons.Rounded.Palette,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    title = stringResource(id = R.string.display_settings),
                    subtitle = stringResource(id = R.string.look_and_feel),
                    onClick = { onNavigateTo(Route.APPEARANCE) }
                )
            }
            // Login X -> open existing cookies page for now
            item {
                SettingRow(
                    icon = Icons.Rounded.SettingsApplications,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    badgeColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    title = "Login X",
                    subtitle = "Login to improve experience",
                    onClick = { onNavigateTo(Route.COOKIE_PROFILE) }
                )
            }
            // Rate us
            item {
                SettingRow(
                    icon = Icons.Rounded.Star,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    badgeColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    title = "Rate Us",
                    subtitle = "Enjoying the app? Give us a 5 star.",
                    onClick = {
                    // Open Play Store listing
                    val pkg = context.packageName
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try { context.startActivity(intent) } catch (e: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
                    }
                    }
                )
            }
            // Privacy policy
            item {
                SettingRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    title = "Privacy Policy",
                    subtitle = "Read privacy policy for using our app.",
                    onClick = {
                    // Open privacy URL (replace with your actual link)
                    val url = "https://example.com/privacy"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
            // Contact / Feedback
            item {
                SettingRow(
                    icon = Icons.Rounded.Email,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    badgeColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    title = "Contact Us",
                    subtitle = "Your feedback matters! Reach out to us",
                    onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback - ${context.getString(R.string.app_name)}")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
            // Version label at bottom
            item {
                androidx.compose.material3.Text(
                    text = "Version " + com.junkfood.seal.BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

    if (showThemeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = stringResource(id = R.string.dark_theme)) },
            text = {
                LazyColumn {
                    item {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.follow_system)) },
                            modifier = Modifier.clickable {
                                PreferenceUtil.modifyDarkThemePreference(FOLLOW_SYSTEM)
                                showThemeDialog = false
                            }
                        )
                    }
                    item {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.on)) },
                            modifier = Modifier.clickable {
                                PreferenceUtil.modifyDarkThemePreference(ON)
                                showThemeDialog = false
                            }
                        )
                    }
                    item {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.off)) },
                            modifier = Modifier.clickable {
                                PreferenceUtil.modifyDarkThemePreference(OFF)
                                showThemeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "Settings - Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Settings - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun SettingsPagePreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = { 
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) { }
        }
    }
}