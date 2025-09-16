package com.rit.twitdownloader.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.app.Activity
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.drawable.ColorDrawable
import com.rit.twitdownloader.App
import com.rit.twitdownloader.R
import com.rit.twitdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.rit.twitdownloader.ui.common.LocalWindowWidthState
import com.rit.twitdownloader.ui.common.Route
import com.rit.twitdownloader.ui.common.animatedComposable
import com.rit.twitdownloader.ui.common.animatedComposableVariant
import com.rit.twitdownloader.ui.common.arg
import com.rit.twitdownloader.ui.common.id
import com.rit.twitdownloader.ui.common.slideInVerticallyComposable
import com.rit.twitdownloader.ui.component.ModernBottomNav
import com.rit.twitdownloader.ui.component.NavTab
import com.rit.twitdownloader.ui.component.FloatingToast
import com.rit.twitdownloader.ui.component.BottomBanner
import com.rit.twitdownloader.download.DownloaderV2
import org.koin.compose.koinInject
import com.rit.twitdownloader.ui.page.command.TaskListPage
import com.rit.twitdownloader.ui.page.command.TaskLogPage
import com.rit.twitdownloader.ui.page.downloadv2.DownloadPageV2
import com.rit.twitdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.rit.twitdownloader.ui.page.settings.network.CookiesViewModel
import com.rit.twitdownloader.ui.page.videolist.VideoListPage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val TAG = "HomeEntry"

private val TopDestinations =
    listOf(Route.HOME, Route.TASK_LIST, Route.SETTINGS_PAGE, Route.DOWNLOADS)

private val BottomTabDestinations =
    listOf(Route.DOWNLOAD_TAB, Route.HOME_TAB, Route.SETTINGS_TAB)

@Composable
fun AppEntry(dialogViewModel: DownloadDialogViewModel) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val view = LocalView.current
    val windowWidth = LocalWindowWidthState.current
    val sheetState by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val cookiesViewModel: CookiesViewModel = koinViewModel()
    val downloader: DownloaderV2 = koinInject()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val versionReport = App.packageInfo.versionName.toString()
    val appName = stringResource(R.string.app_name)
    val scope = rememberCoroutineScope()

    // Keep status bar painted and consistent across tab transitions
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            val topColor = Color.Black
            window.statusBarColor = topColor.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
            window.setBackgroundDrawable(ColorDrawable(topColor.toArgb()))
        }
    }

    val onNavigateBack: () -> Unit = {
        with(navController) {
            if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                popBackStack()
            }
        }
    }

    if (sheetState is DownloadDialogViewModel.SheetState.Configure) {
        if (navController.currentDestination?.route != Route.HOME) {
            navController.popBackStack(route = Route.HOME, inclusive = false, saveState = true)
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var currentTopDestination by rememberSaveable { mutableStateOf(currentRoute) }

    // Determine if we're in a bottom tab route
    val isBottomTabRoute = currentRoute in BottomTabDestinations

    LaunchedEffect(currentRoute) {
        if (currentRoute in TopDestinations) {
            currentTopDestination = currentRoute
        }
    }

    var showStartedToast by remember { mutableStateOf(false) }
    var showBanner by remember { mutableStateOf(false) }
    var bannerText by remember { mutableStateOf("") }
    var unseenCompleted by remember { mutableStateOf(0) }
    val seenCompletedIds = remember { mutableSetOf<String>() }
    val seenStartedIds = remember { mutableSetOf<String>() }
    val seenEnqueuedIds = remember { mutableSetOf<String>() }

    // Observe task state map to detect starts/completions/errors
    LaunchedEffect(Unit) {
        // naive polling via snapshot - replace with a proper flow if exposed later
        while (true) {
            val states = downloader.getTaskStateMap().toMap()
            // Increment badge only when a brand new task is observed
            states.forEach { (task, state) ->
                val id = task.id
                if (!seenEnqueuedIds.contains(id) && state.downloadState is com.rit.twitdownloader.download.Task.DownloadState.Idle) {
                    seenEnqueuedIds.add(id) // brand new item observed
                    // no badge increment here; only on completion
                }
                // Only show start when task first transitions to Running (not on FetchingInfo retries)
                if (!seenStartedIds.contains(id) && state.downloadState is com.rit.twitdownloader.download.Task.DownloadState.Running) {
                    seenStartedIds.add(id)
                    showStartedToast = true
                }
                if (state.downloadState is com.rit.twitdownloader.download.Task.DownloadState.Completed && !seenCompletedIds.contains(id)) {
                    seenCompletedIds.add(id)
                    unseenCompleted += 1
                    bannerText = "Download completed"
                    showBanner = true
                }
                if (state.downloadState is com.rit.twitdownloader.download.Task.DownloadState.Error && !seenCompletedIds.contains(id)) {
                    seenCompletedIds.add(id) // prevent repeat
                    bannerText = "Download failed"
                    showBanner = true
                }
            }
            kotlinx.coroutines.delay(400)
        }
    }

    Scaffold(
        bottomBar = {
            if (isBottomTabRoute) {
                val selectedTab = when (currentRoute) {
                    Route.HOME_TAB -> NavTab.Home
                    Route.DOWNLOAD_TAB -> NavTab.Downloads
                    Route.SETTINGS_TAB -> NavTab.Settings
                    else -> NavTab.Home
                }
                ModernBottomNav(
                    selectedTab = selectedTab,
                    downloadsBadgeCount = unseenCompleted,
                    onSelect = { tab ->
                        val route = when (tab) {
                            NavTab.Home -> Route.HOME_TAB
                            NavTab.Downloads -> Route.DOWNLOAD_TAB
                            NavTab.Settings -> Route.SETTINGS_TAB
                        }
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                popUpTo(route = Route.HOME_TAB) { inclusive = false }
                            }
                            if (tab == NavTab.Downloads) unseenCompleted = 0
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Apply only start/top/end padding so content can extend beneath the bottom bar
        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                )
        ) {
            NavigationDrawer(
                windowWidth = windowWidth,
                drawerState = drawerState,
                currentRoute = currentRoute,
                currentTopDestination = currentTopDestination,
                showQuickSettings = true,
                gesturesEnabled = false,
                onDismissRequest = { drawerState.close() },
                onNavigateToRoute = {
                    if (currentRoute != it) {
                        navController.navigate(it) {
                            launchSingleTop = true
                            popUpTo(route = Route.HOME)
                        }
                    }
                },
                footer = {
                    Text(
                        appName + "\n" + versionReport + "\n" + currentRoute,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                },
            ) {
                // Crossfade tab destinations to avoid flashes while keeping the top area stable
                androidx.compose.animation.Crossfade(
                    targetState = navController.currentBackStackEntryAsState().value?.destination?.route,
                    animationSpec = androidx.compose.animation.core.tween(150)
                ) { routeKey ->
                key(routeKey) {
                NavHost(
                    modifier = Modifier.align(Alignment.Center),
                    navController = navController,
                    startDestination = Route.HOME_TAB,
                ) {
                    animatedComposable(Route.HOME) {
                        DownloadPageV2(
                            dialogViewModel = dialogViewModel,
                            onMenuOpen = {
                                view.slightHapticFeedback()
                                scope.launch { drawerState.open() }
                            },
                        )
                    }
                    animatedComposable(Route.DOWNLOADS) { VideoListPage { onNavigateBack() } }
                    animatedComposableVariant(Route.TASK_LIST) {
                        TaskListPage(
                            onNavigateBack = onNavigateBack,
                            onNavigateToDetail = { navController.navigate(Route.TASK_LOG id it) },
                        )
                    }
                    slideInVerticallyComposable(
                        Route.TASK_LOG arg Route.TASK_HASHCODE,
                        arguments = listOf(navArgument(Route.TASK_HASHCODE) { type = NavType.IntType }),
                    ) {
                        TaskLogPage(
                            onNavigateBack = onNavigateBack,
                            taskHashCode = it.arguments?.getInt(Route.TASK_HASHCODE) ?: -1,
                        )
                    }

                    // Bottom tab routes
                    animatedComposable(Route.HOME_TAB) {
                        HomeTabScreen(
                            dialogViewModel = dialogViewModel
                        )
                    }
                    animatedComposable(Route.DOWNLOAD_TAB) {
                        DownloadPageV2(
                            dialogViewModel = dialogViewModel
                        )
                    }
                    animatedComposable(Route.SETTINGS_TAB) {
                        SettingsTabScreen(
                            onNavigateTo = { route ->
                                navController.navigate(route = route) { launchSingleTop = true }
                            }
                        )
                    }

                    settingsGraph(
                        onNavigateBack = onNavigateBack,
                        onNavigateTo = { route -> navController.navigate(route = route) { launchSingleTop = true } },
                        cookiesViewModel = cookiesViewModel
                    )
                } // <-- closes NavHost inside key
                } // <-- closes key
                }

            } // <-- closes NavigationDrawer content lambda (MISSING brace was added here)

            // Global overlays
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingToast(
                    message = "Download started...",
                    visible = showStartedToast,
                    onHide = { showStartedToast = false },
                    bottomPadding = 84.dp
                )
                BottomBanner(
                    message = bannerText,
                    visible = showBanner,
                    onClick = {
                        showBanner = false
                        if (currentRoute != Route.DOWNLOAD_TAB) {
                            navController.navigate(Route.DOWNLOAD_TAB) { launchSingleTop = true }
                            unseenCompleted = 0
                        }
                    },
                    onHide = { showBanner = false }
                )
            }

            AppUpdater()
            YtdlpUpdater()
        }
    }
}

// settingsGraph is defined in AppEntryExtras.kt

// Previews moved to AppEntryPreviews.kt

