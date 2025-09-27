package com.rit.twitdownloader

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.rit.twitdownloader.App.Companion.context
import com.rit.twitdownloader.ui.common.LocalDarkTheme
import com.rit.twitdownloader.ui.common.SettingsProvider
import com.rit.twitdownloader.ui.page.AppEntry
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.rit.twitdownloader.ui.theme.SealTheme
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.matchUrlFromSharedText
import com.rit.twitdownloader.util.SharedUrlBus
import com.rit.twitdownloader.util.setLanguage
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    private val dialogViewModel: DownloadDialogViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language for all API levels to ensure consistent locale handling
        runBlocking { 
            if (Build.VERSION.SDK_INT < 33) {
                setLanguage(PreferenceUtil.getLocaleFromPreference())
            } else {
                // For API 33+, ensure we have a proper locale set
                val savedLocale = PreferenceUtil.getLocaleFromPreference()
                if (savedLocale != null) {
                    setLanguage(savedLocale)
                }
            }
        }
        enableEdgeToEdge()

        context = this.baseContext
        
        setContent {
            KoinContext {
                val windowSizeClass = calculateWindowSizeClass(this)
                SettingsProvider(windowWidthSizeClass = windowSizeClass.widthSizeClass) {
                    SealTheme(
                        darkTheme = LocalDarkTheme.current.isDarkTheme(),
                        isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    ) {
                        AppEntry(dialogViewModel = dialogViewModel)
                    }
                }
            }
        }

        // Handle the initial intent as well (cold start via Share or View)
        intent?.let { initialIntent ->
            initialIntent.getSharedURL()?.let { url ->
                if (!url.isNullOrEmpty()) {
                    sharedUrlCached = url
                    SharedUrlBus.emit(url)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val url = intent.getSharedURL()
        if (url != null) {
            // Navigate to Home tab and paste URL into Home input; no auto-download
            sharedUrlCached = url
            SharedUrlBus.emit(url)
        }
    }

    private fun Intent.getSharedURL(): String? {
        val intent = this

        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedContent ->
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    matchUrlFromSharedText(sharedContent).also { matchedUrl ->
                        if (sharedUrlCached != matchedUrl) {
                            sharedUrlCached = matchedUrl
                        }
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private var sharedUrlCached = ""
    }
}

