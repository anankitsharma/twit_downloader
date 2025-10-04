package com.rit.twitdownloader.ads

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobManager(private val context: Context) {
    
    companion object {
        // Test Ad Unit IDs - Replace with your actual Ad Unit IDs
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        
        // Production Ad Unit IDs - Replace with your actual Ad Unit IDs
        const val BANNER_AD_UNIT_ID = "ca-app-pub-5222250264081212/1234567890"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5222250264081212/1234567890"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-5222250264081212/1234567890"
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    init {
        MobileAds.initialize(context) {}
    }
    
    fun loadInterstitialAd() {
        // Ensure we're on the main thread
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                loadInterstitialAdInternal()
            }
            return
        }
        loadInterstitialAdInternal()
    }
    
    private fun loadInterstitialAdInternal() {
        val adRequest = AdRequest.Builder().build()
        
        android.util.Log.d("AdMobManager", "Loading interstitial ad with ID: $TEST_INTERSTITIAL_AD_UNIT_ID")
        android.util.Log.d("AdMobManager", "Ad request details: ${adRequest.keywords}")
        
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID, // Use TEST_INTERSTITIAL_AD_UNIT_ID for testing
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d("AdMobManager", "Interstitial ad loaded successfully at ${System.currentTimeMillis()}")
                    interstitialAd = ad
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e("AdMobManager", "Interstitial ad failed to load: ${error.message}")
                    android.util.Log.e("AdMobManager", "Error code: ${error.code}, domain: ${error.domain}")
                    interstitialAd = null
                }
            }
        )
    }
    
    fun showInterstitialAd(activity: android.app.Activity, onAdClosed: () -> Unit = {}) {
        if (interstitialAd != null) {
            android.util.Log.d("AdMobManager", "Showing interstitial ad")
            interstitialAd?.let { ad ->
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        android.util.Log.d("AdMobManager", "Interstitial ad dismissed")
                        interstitialAd = null
                        onAdClosed()
                    }
                    
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        android.util.Log.e("AdMobManager", "Interstitial ad failed to show: ${error.message}")
                        interstitialAd = null
                        onAdClosed()
                    }
                }
                ad.show(activity)
            }
        } else {
            android.util.Log.w("AdMobManager", "No interstitial ad available to show")
            onAdClosed()
        }
    }
    
    fun loadRewardedAd() {
        // Ensure we're on the main thread
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                loadRewardedAdInternal()
            }
            return
        }
        loadRewardedAdInternal()
    }
    
    private fun loadRewardedAdInternal() {
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID, // Use TEST_REWARDED_AD_UNIT_ID for testing
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }
    
    fun showRewardedAd(onRewardEarned: () -> Unit = {}, onAdClosed: () -> Unit = {}) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    onAdClosed()
                }
            }
            
            ad.show(context as android.app.Activity) { rewardItem ->
                onRewardEarned()
            }
        } ?: run {
            onAdClosed()
        }
    }
    
    fun isInterstitialAdLoaded(): Boolean = interstitialAd != null
    fun isRewardedAdLoaded(): Boolean = rewardedAd != null
}
