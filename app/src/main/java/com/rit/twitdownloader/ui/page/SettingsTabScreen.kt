package com.rit.twitdownloader.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    XHeaderScaffold(title = stringResource(R.string.settings)) {
        SettingsPage(
            onNavigateBack = {},
            onNavigateTo = onNavigateTo
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

