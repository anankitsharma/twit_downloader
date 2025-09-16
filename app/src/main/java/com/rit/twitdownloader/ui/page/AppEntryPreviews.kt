package com.rit.twitdownloader.ui.page

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.rit.twitdownloader.ui.component.ModernBottomNav
import com.rit.twitdownloader.ui.component.NavTab
import com.rit.twitdownloader.ui.page.downloadv2.DownloadPageImplV2

@Composable
@Preview(name = "App Entry - Home Tab", showBackground = true)
private fun AppEntryHomePreview() {
    MaterialTheme {
        HomeTabScreen(
            dialogViewModel = null
        )
    }
}

@Composable
@Preview(name = "App Entry - Download Tab", showBackground = true)
private fun AppEntryDownloadPreview() {
    MaterialTheme {
        DownloadPageImplV2(
            taskDownloadStateMap = remember { mutableStateMapOf() },
            onActionPost = { _, _ -> }
        )
    }
}

@Composable
@Preview(name = "App Entry - Settings Tab", showBackground = true)
private fun AppEntrySettingsPreview() {
    MaterialTheme {
        SettingsTabScreen(
            onNavigateTo = {}
        )
    }
}

@Composable
@Preview(name = "Bottom Navigation Bar", showBackground = true)
private fun AppEntryBottomNavPreview() {
    MaterialTheme {
        ModernBottomNav(selectedTab = NavTab.Home, onSelect = {})
    }
}

@Composable
@Preview(name = "Complete App Structure", showBackground = true)
private fun AppEntryCompletePreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = { ModernBottomNav(selectedTab = NavTab.Home, onSelect = {}) }
        ) { paddingValues ->
            HomeTabScreen(
                dialogViewModel = null,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}



