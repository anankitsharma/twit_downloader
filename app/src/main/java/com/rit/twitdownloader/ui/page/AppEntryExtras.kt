package com.rit.twitdownloader.ui.page

import android.webkit.CookieManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.rit.twitdownloader.ui.common.Route
import com.rit.twitdownloader.ui.common.animatedComposable
import com.rit.twitdownloader.ui.common.arg
import com.rit.twitdownloader.ui.common.id
import com.rit.twitdownloader.ui.component.ModernBottomNav
import com.rit.twitdownloader.ui.component.NavTab
import com.rit.twitdownloader.ui.page.settings.SettingsPage
import com.rit.twitdownloader.ui.page.settings.about.AboutPage
import com.rit.twitdownloader.ui.page.settings.about.CreditsPage
import com.rit.twitdownloader.ui.page.settings.about.SponsorsPage
import com.rit.twitdownloader.ui.page.settings.about.UpdatePage
import com.rit.twitdownloader.ui.page.settings.appearance.AppearancePreferences
import com.rit.twitdownloader.ui.page.settings.appearance.DarkThemePreferences
import com.rit.twitdownloader.ui.page.settings.appearance.LanguagePage
import com.rit.twitdownloader.ui.page.settings.command.TemplateEditPage
import com.rit.twitdownloader.ui.page.settings.command.TemplateListPage
import com.rit.twitdownloader.ui.page.settings.directory.DownloadDirectoryPreferences
import com.rit.twitdownloader.ui.page.settings.format.DownloadFormatPreferences
import com.rit.twitdownloader.ui.page.settings.format.SubtitlePreference
import com.rit.twitdownloader.ui.page.settings.general.GeneralDownloadPreferences
import com.rit.twitdownloader.ui.page.settings.interaction.InteractionPreferencePage
import com.rit.twitdownloader.ui.page.settings.network.CookieProfilePage
import com.rit.twitdownloader.ui.page.settings.network.CookiesViewModel
import com.rit.twitdownloader.ui.page.settings.network.NetworkPreferences
import com.rit.twitdownloader.ui.page.settings.network.WebViewPage
import com.rit.twitdownloader.ui.page.settings.troubleshooting.TroubleShootingPage

fun NavGraphBuilder.settingsGraph(
    onNavigateBack: () -> Unit,
    onNavigateTo: (route: String) -> Unit,
    cookiesViewModel: CookiesViewModel,
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        animatedComposable(Route.DOWNLOAD_DIRECTORY) { DownloadDirectoryPreferences(onNavigateBack) }
        animatedComposable(Route.SETTINGS_PAGE) { SettingsPage(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo) }
        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(onNavigateBack = { onNavigateBack() }) { onNavigateTo(Route.TEMPLATE) }
        }
        animatedComposable(Route.DOWNLOAD_FORMAT) {
            DownloadFormatPreferences(onNavigateBack = onNavigateBack) { onNavigateTo(Route.SUBTITLE_PREFERENCES) }
        }
        animatedComposable(Route.SUBTITLE_PREFERENCES) { SubtitlePreference { onNavigateBack() } }
        animatedComposable(Route.ABOUT) {
            AboutPage(
                onNavigateBack = onNavigateBack,
                onNavigateToCreditsPage = { onNavigateTo(Route.CREDITS) },
                onNavigateToUpdatePage = { onNavigateTo(Route.AUTO_UPDATE) },
                onNavigateToDonatePage = { onNavigateTo(Route.DONATE) },
            )
        }
        animatedComposable(Route.DONATE) { SponsorsPage(onNavigateBack) }
        animatedComposable(Route.CREDITS) { CreditsPage(onNavigateBack) }
        animatedComposable(Route.AUTO_UPDATE) { UpdatePage(onNavigateBack) }
        animatedComposable(Route.APPEARANCE) { AppearancePreferences(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo) }
        animatedComposable(Route.INTERACTION) { InteractionPreferencePage(onBack = onNavigateBack) }
        animatedComposable(Route.LANGUAGES) { LanguagePage { onNavigateBack() } }
        animatedComposable(Route.TEMPLATE) {
            TemplateListPage(onNavigateBack = onNavigateBack) { onNavigateTo(Route.TEMPLATE_EDIT id it) }
        }
        animatedComposable(
            Route.TEMPLATE_EDIT arg Route.TEMPLATE_ID,
            arguments = listOf(navArgument(Route.TEMPLATE_ID) { type = NavType.IntType }),
        ) {
            TemplateEditPage(onNavigateBack, it.arguments?.getInt(Route.TEMPLATE_ID) ?: -1)
        }
        animatedComposable(Route.DARK_THEME) { DarkThemePreferences { onNavigateBack() } }
        animatedComposable(Route.NETWORK_PREFERENCES) {
            NetworkPreferences(
                navigateToCookieProfilePage = { onNavigateTo(Route.COOKIE_PROFILE) }
            ) { onNavigateBack() }
        }
        animatedComposable(Route.COOKIE_PROFILE) {
            CookieProfilePage(
                cookiesViewModel = cookiesViewModel,
                navigateToCookieGeneratorPage = { onNavigateTo(Route.COOKIE_GENERATOR_WEBVIEW) },
            ) { onNavigateBack() }
        }
        animatedComposable(Route.COOKIE_GENERATOR_WEBVIEW) {
            WebViewPage(cookiesViewModel = cookiesViewModel) {
                onNavigateBack()
                CookieManager.getInstance().flush()
            }
        }
        animatedComposable(Route.TROUBLESHOOTING) { TroubleShootingPage(onNavigateTo = onNavigateTo, onBack = onNavigateBack) }
    }
}

@Composable
@Preview(name = "Bottom Navigation Bar", showBackground = true)
private fun AppEntryBottomNavPreview2() {
    MaterialTheme { ModernBottomNav(selectedTab = NavTab.Home, onSelect = {}) }
}



