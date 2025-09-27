package com.rit.twitdownloader.ui.page.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues // Added import
import androidx.compose.foundation.layout.ExperimentalLayoutApi // Added import
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
// import androidx.compose.material.icons.outlined.Language // Commented out - not used in English-only build
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
import androidx.compose.ui.graphics.Color
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Added ExperimentalLayoutApi
@Composable
fun SettingsPage(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    // Minimal settings: remove battery/sponsor flows, keep simple items only

    val darkThemePreference = LocalDarkTheme.current
    val isDark = darkThemePreference.isDarkTheme()
    
    // Get string resources outside of onClick lambdas
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val contactEmail = stringResource(R.string.contact_email)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)), // Light gray background like in the image
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 16.dp,
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {

            // Display Language (top-level) - Commented out for English-only build
            // item {
            //     val langSubtitle = "English" // Default fallback
            //     SettingRow(
            //         icon = Icons.Outlined.Language,
            //         iconTint = MaterialTheme.colorScheme.secondary,
            //         badgeColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            //         title = stringResource(id = R.string.language),
            //         subtitle = langSubtitle,
            //         onClick = { onNavigateTo(Route.LANGUAGES) }
            //     )
            // }
            // Dark theme toggle - modern card design
            item {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon in circular background - Theme color (purple)
                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape),
                            color = Color(0xFF9C27B0), // Purple for theme
                            shape = androidx.compose.foundation.shape.CircleShape,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            androidx.compose.foundation.layout.Box(
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Title and subtitle
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text(
                                text = stringResource(id = R.string.dark_theme),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF333333)
                            )
                            androidx.compose.material3.Text(
                                text = "Toggle the app theme",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = Color(0xFF666666)
                            )
                        }
                        
                        // Toggle switch
                        androidx.compose.material3.Switch(
                            checked = isDark,
                            onCheckedChange = { checked ->
                                PreferenceUtil.modifyDarkThemePreference(if (checked) ON else OFF)
                            }
                        )
                    }
                }
            }
            // Login X -> open existing cookies page for now
            item {
                SettingRow(
                    icon = Icons.Rounded.SettingsApplications,
                    iconTint = Color.White,
                    badgeColor = Color(0xFF1DA1F2), // Twitter blue for login
                    title = stringResource(R.string.login_x),
                    subtitle = stringResource(R.string.login_subtitle),
                    onClick = { onNavigateTo(Route.COOKIE_PROFILE) }
                )
            }
            // Rate us
            item {
                SettingRow(
                    icon = Icons.Rounded.Star,
                    iconTint = Color.White,
                    badgeColor = Color(0xFFFFB300), // Gold/amber for star rating
                    title = stringResource(R.string.rate_us),
                    subtitle = stringResource(R.string.rate_us_subtitle),
                    onClick = {
                    // Open Play Store listing
                    val pkg = context.packageName
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                        context.startActivity(Intent.createChooser(intent, "Open with"))
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                        context.startActivity(Intent.createChooser(webIntent, "Open with"))
                    }
                    }
                )
            }
            // Privacy policy
            item {
                SettingRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = Color.White,
                    badgeColor = Color(0xFF4CAF50), // Green for security/privacy
                    title = stringResource(R.string.privacy_policy),
                    subtitle = stringResource(R.string.privacy_policy_subtitle),
                    onClick = {
                    // Open privacy policy URL
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                    }
                )
            }
            // Contact / Feedback
            item {
                SettingRow(
                    icon = Icons.Rounded.Email,
                    iconTint = Color.White,
                    badgeColor = Color(0xFF2196F3), // Blue for email/contact
                    title = stringResource(R.string.contact_us),
                    subtitle = stringResource(R.string.contact_us_subtitle),
                    onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(contactEmail))
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback - ${context.getString(R.string.app_name)}")
                    }
                    try { context.startActivity(Intent.createChooser(intent, "Send email")) } catch (_: Exception) {}
                    }
                )
            }
            // Version footer at bottom with heart icon
            item {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Version ${com.rit.twitdownloader.BuildConfig.VERSION_NAME} • Made with ",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFFAAAAAA)
                    )
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE91E63), // Pink heart color
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

    // Dialog removed; toggle is inline now
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
