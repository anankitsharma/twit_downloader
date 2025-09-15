package com.junkfood.seal.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = currentRoute == Route.HOME_TAB,
            onClick = { onNavigateToRoute(Route.HOME_TAB) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = stringResource(R.string.download)
                )
            },
            label = { Text(stringResource(R.string.download)) },
            selected = currentRoute == Route.DOWNLOAD_TAB,
            onClick = { onNavigateToRoute(Route.DOWNLOAD_TAB) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            selected = currentRoute == Route.SETTINGS_TAB,
            onClick = { onNavigateToRoute(Route.SETTINGS_TAB) }
        )
    }
}

@Composable
@Preview(name = "Bottom Navigation Bar", showBackground = true)
private fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        currentRoute = Route.HOME_TAB,
        onNavigateToRoute = {}
    )
}
