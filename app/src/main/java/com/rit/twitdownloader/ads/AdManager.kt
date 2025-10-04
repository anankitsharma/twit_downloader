package com.rit.twitdownloader.ads

import android.content.Context
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.PreferenceUtil.getInt
import com.rit.twitdownloader.util.PreferenceUtil.updateInt
import com.rit.twitdownloader.util.PreferenceUtil.getBoolean
import com.rit.twitdownloader.util.PreferenceUtil.updateBoolean

class AdManager(private val context: Context) {
    
    private val adMobManager = AdMobManager(context)
    private val prefs = PreferenceUtil
    
    companion object {
        private const val APP_LAUNCHED_BEFORE_KEY = "app_launched_before"
    }
    
    init {
        // Don't load ads in init - will be loaded later on main thread
    }
    
    fun shouldShowSplashAd(): Boolean {
        // Check if ads are enabled and not first launch
        val adsEnabled = isAdsEnabled()
        val launchedBefore = APP_LAUNCHED_BEFORE_KEY.getBoolean()
        val shouldShow = adsEnabled && launchedBefore
        
        android.util.Log.d("AdManager", "shouldShowSplashAd: adsEnabled=$adsEnabled, launchedBefore=$launchedBefore, shouldShow=$shouldShow")
        
        return shouldShow
    }
    
    fun markAppLaunched() {
        android.util.Log.d("AdManager", "markAppLaunched: Setting app as launched")
        APP_LAUNCHED_BEFORE_KEY.updateBoolean(true)
    }
    
    fun resetFirstLaunch() {
        android.util.Log.d("AdManager", "resetFirstLaunch: Resetting first launch flag")
        APP_LAUNCHED_BEFORE_KEY.updateBoolean(false)
        
        // Force a small delay to ensure the value is written
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        
        // Verify the reset worked
        val currentValue = APP_LAUNCHED_BEFORE_KEY.getBoolean()
        android.util.Log.d("AdManager", "resetFirstLaunch: Verification - launchedBefore is now: $currentValue")
    }
    
    fun loadSplashAd() {
        if (isAdsEnabled()) {
            adMobManager.loadInterstitialAd()
        }
    }
    
    fun isSplashAdReady(): Boolean {
        val isReady = adMobManager.isInterstitialAdLoaded()
        android.util.Log.d("AdManager", "isSplashAdReady: $isReady at ${System.currentTimeMillis()}")
        return isReady
    }
    
    fun showSplashAd(activity: android.app.Activity, onComplete: () -> Unit) {
        if (adMobManager.isInterstitialAdLoaded()) {
            adMobManager.showInterstitialAd(activity) {
                // Reload ad for next time
                adMobManager.loadInterstitialAd()
                onComplete()
            }
        } else {
            // Ad not ready, go to main app
            onComplete()
        }
    }
    
    fun showRewardedAd(onRewardEarned: () -> Unit = {}) {
        if (adMobManager.isRewardedAdLoaded()) {
            adMobManager.showRewardedAd(
                onRewardEarned = onRewardEarned
            ) {
                // Reload ad after showing
                adMobManager.loadRewardedAd()
            }
        } else {
            // Load ad for next time
            adMobManager.loadRewardedAd()
        }
    }
    
    fun loadAds() {
        if (isAdsEnabled()) {
            adMobManager.loadInterstitialAd()
            adMobManager.loadRewardedAd()
        }
    }
    
    private fun isAdsEnabled(): Boolean {
        return "ads_enabled".getBoolean(true)
    }
}
