package com.rit.twitdownloader.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.rit.twitdownloader.ads.isAdsEnabled

@Composable
fun BannerAdView(
    adUnitId: String = AdMobManager.TEST_BANNER_AD_UNIT_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    
    // Only show ads if user has consented
    if (!isAdsEnabled(context)) {
        return
    }
    
    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                    
                    adView = this
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
