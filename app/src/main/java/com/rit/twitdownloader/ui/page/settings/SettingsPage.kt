package com.rit.twitdownloader.ui.page.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.common.Route
import com.rit.twitdownloader.ui.component.SettingRow
import com.rit.twitdownloader.ui.common.LocalDarkTheme
import com.rit.twitdownloader.util.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.rit.twitdownloader.util.DarkThemePreference.Companion.OFF
import com.rit.twitdownloader.util.DarkThemePreference.Companion.ON
import com.rit.twitdownloader.util.PreferenceUtil

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
            // Dark theme (top-level; replaces nested display/appearance page)
            item {
                SettingRow(
                    icon = Icons.Rounded.Palette,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    title = stringResource(id = R.string.dark_theme),
                    subtitle = darkThemePreference.getDarkThemeDesc(),
                    onClick = { showThemeDialog = true }
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
                    text = "Version " + com.rit.twitdownloader.BuildConfig.VERSION_NAME,
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
