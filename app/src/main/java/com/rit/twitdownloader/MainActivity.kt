package com.rit.twitdownloader

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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
import com.rit.twitdownloader.ui.component.RatingDialogFragment
import com.rit.twitdownloader.util.RatingManager
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    private val dialogViewModel: DownloadDialogViewModel by viewModel()
    private lateinit var ratingManager: RatingManager

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
        
        // Official Android 15+ edge-to-edge configuration
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Set status bar to black for consistent appearance across all devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.BLACK
        }
        
        // Set navigation bar to transparent for edge-to-edge display
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        // Disable contrast enforcement for transparent navigation bar (official recommendation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        context = this.baseContext
        
        // Initialize rating manager
        ratingManager = RatingManager(this)
        
        // Track app launch
        ratingManager.trackAppLaunch()
        
        // Check if we should show rating prompt (based on launches)
        checkAndShowRatingPrompt()
        
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

    private fun checkAndShowRatingPrompt() {
        println("MainActivity: checkAndShowRatingPrompt() called")
        val shouldShow = ratingManager.shouldShowRatingPrompt()
        println("MainActivity: shouldShowRatingPrompt() returned: $shouldShow")
        
        if (shouldShow) {
            println("MainActivity: Showing rating dialog...")
            showRatingDialog()
        } else {
            println("MainActivity: Not showing rating prompt. Debug info: ${ratingManager.getDebugInfo()}")
        }
    }
    
    private fun showRatingDialog() {
        // Don't show if already showing
        if (supportFragmentManager.findFragmentByTag("rating_dialog") != null) {
            return
        }
        
        // Notify rating manager that prompt is being shown
        ratingManager.onRatingPromptShown()
        
        val dialog = RatingDialogFragment.newInstance()
        
        dialog.setOnRatingSubmittedListener { rating ->
            println("User rated app: $rating stars")
            ratingManager.onUserRated()
        }
        
        dialog.setOnDismissedListener {
            println("User dismissed rating dialog (Later)")
            ratingManager.onUserDismissed()
        }
        
        dialog.show(supportFragmentManager, "rating_dialog")
    }
    
    /**
     * Call this method when a download completes successfully
     * This should be called from your download completion handler
     */
    fun onDownloadCompleted() {
        ratingManager.trackSuccessfulDownload()
        checkAndShowRatingPrompt()
    }
    
    /**
     * Debug method to simulate app launch
     * Call this for testing the rating system
     */
    fun simulateAppLaunch() {
        ratingManager.trackAppLaunch()
        checkAndShowRatingPrompt()
    }
    
    /**
     * Test method to simulate multiple launches quickly
     * Call this for testing the rating system
     */
    fun testRatingSystem() {
        // Reset data first
        ratingManager.resetAll()
        
        // Simulate 3 launches - should trigger first prompt
        println("MainActivity: Testing rating system...")
        println("MainActivity: Simulating 3 launches...")
        for (i in 1..3) {
            ratingManager.trackAppLaunch()
        }
        checkAndShowRatingPrompt()
    }
    
    /**
     * Debug method to reset rating data
     * Call this for testing the rating system
     */
    fun resetRatingData() {
        ratingManager.resetAll()
        println("MainActivity: Rating data reset. Debug info: ${ratingManager.getDebugInfo()}")
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

