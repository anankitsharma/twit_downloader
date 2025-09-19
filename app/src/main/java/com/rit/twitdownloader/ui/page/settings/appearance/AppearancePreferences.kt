package com.rit.twitdownloader.ui.page.settings.appearance

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.android.material.color.DynamicColors
import com.rit.twitdownloader.R
import com.rit.twitdownloader.download.Task
import com.rit.twitdownloader.ui.common.LocalDarkTheme
 
import com.rit.twitdownloader.ui.common.Route
import com.rit.twitdownloader.ui.component.BackButton
import com.rit.twitdownloader.ui.component.PreferenceItem
import com.rit.twitdownloader.ui.component.PreferenceSwitch
import com.rit.twitdownloader.ui.component.PreferenceSwitchWithDivider
import com.rit.twitdownloader.ui.page.downloadv2.ActionButton
import com.rit.twitdownloader.ui.page.downloadv2.CardStateIndicator
import com.rit.twitdownloader.ui.page.downloadv2.VideoCardV2
import com.rit.twitdownloader.util.DarkThemePreference.Companion.OFF
import com.rit.twitdownloader.util.DarkThemePreference.Companion.ON
import com.rit.twitdownloader.util.PreferenceUtil
import com.rit.twitdownloader.util.STYLE_MONOCHROME
import com.rit.twitdownloader.util.STYLE_TONAL_SPOT
import com.rit.twitdownloader.util.paletteStyles
import com.rit.twitdownloader.util.toDisplayName
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import com.kyant.monet.a1
import com.kyant.monet.a2
import com.kyant.monet.a3
import io.material.hct.Hct
import java.util.Locale
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(modifier = Modifier, text = stringResource(id = R.string.look_and_feel))
                },
                navigationIcon = { BackButton(onNavigateBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(it)) {
                val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    isChecked = isDarkTheme,
                    onChecked = {
                        PreferenceUtil.modifyDarkThemePreference(if (isDarkTheme) OFF else ON)
                    },
                )
                // Language selection removed - English only to reduce app size
                // PreferenceItem(
                //     title = stringResource(R.string.language),
                //     icon = Icons.Outlined.Language,
                //     description = Locale.getDefault().toDisplayName(),
                // ) {
                //     onNavigateTo(Route.LANGUAGES)
                // }
            }
        },
    )
}

@Composable
fun RowScope.ColorButtons(color: Color) {}

