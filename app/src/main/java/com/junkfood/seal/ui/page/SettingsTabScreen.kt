package com.junkfood.seal.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.ui.page.settings.SettingsPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabScreen(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" ") },
                navigationIcon = {
                    IconButton(onClick = { /* Menu action if needed */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsPage(
                onNavigateBack = {}, // No back navigation needed in tab
                onNavigateTo = onNavigateTo
            )
        }
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
