package com.rit.twitdownloader.ui.page

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.page.settings.SettingsPage
import com.rit.twitdownloader.ui.component.XHeaderScaffold
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabScreen(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showInstructionScreen by remember { mutableStateOf(false) }
    
    XHeaderScaffold(
        title = stringResource(R.string.settings),
        onShareClick = {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "🚀 Download any X/Twitter video instantly!\nI just found this amazing app:\nhttps://play.google.com/store/apps/details?id=com.rit.twitdownloader\nTry it once, you'll wonder how you lived without it. 😍")
                putExtra(Intent.EXTRA_SUBJECT, "XDown: X / Twitter Video Saver")
            }
            val chooserIntent = Intent.createChooser(shareIntent, "Share XDown")
            context.startActivity(chooserIntent)
        },
        onInfoClick = {
            showInstructionScreen = true
        }
    ) {
        SettingsPage(
            onNavigateBack = {},
            onNavigateTo = onNavigateTo
        )
    }

    if (showInstructionScreen) {
        InstructionScreen(
            onBackClick = { showInstructionScreen = false },
            onCloseClick = { showInstructionScreen = false }
        )
    }
}

@Composable
@Preview(name = "Settings Tab", showBackground = true)
private fun SettingsTabScreenPreview() {
    MaterialTheme {
        SettingsTabScreen(
            onNavigateTo = {}
        )
    }
}

