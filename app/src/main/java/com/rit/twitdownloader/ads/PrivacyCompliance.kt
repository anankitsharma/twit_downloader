package com.rit.twitdownloader.ads

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rit.twitdownloader.R
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.PreferenceUtil.getBoolean
import com.rit.twitdownloader.util.PreferenceUtil.updateBoolean

@Composable
fun PrivacyConsentDialog(
    onConsentGiven: () -> Unit,
    onConsentDenied: () -> Unit
) {
    val context = LocalContext.current
    val hasShownConsent = remember { 
        "privacy_consent_shown".getBoolean(false)
    }
    
    if (!hasShownConsent) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissal without choice */ },
            title = {
                Text(
                    text = "Privacy & Ads",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "This app uses ads to support development. We may collect data to show relevant ads.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "You can opt out of personalized ads in your device settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        "privacy_consent_shown".updateBoolean(true)
                        "ads_enabled".updateBoolean(true)
                        onConsentGiven()
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        "privacy_consent_shown".updateBoolean(true)
                        "ads_enabled".updateBoolean(false)
                        onConsentDenied()
                    }
                ) {
                    Text("Opt Out")
                }
            }
        )
    }
}

fun isAdsEnabled(context: Context): Boolean {
    return "ads_enabled".getBoolean(true)
}
