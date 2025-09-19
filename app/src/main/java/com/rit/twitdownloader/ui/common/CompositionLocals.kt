package com.rit.twitdownloader.ui.common

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.rit.twitdownloader.ui.theme.DEFAULT_SEED_COLOR
import com.rit.twitdownloader.ui.theme.FixedColorRoles
import com.rit.twitdownloader.util.DarkThemePreference
import com.rit.twitdownloader.util.PreferenceUtil
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes

val LocalDarkTheme = compositionLocalOf { DarkThemePreference() }
val LocalWindowWidthState = staticCompositionLocalOf { WindowWidthSizeClass.Compact }
val LocalFixedColorRoles = staticCompositionLocalOf {
    FixedColorRoles.fromColorSchemes(
        lightColors = lightColorScheme(),
        darkColors = darkColorScheme(),
    )
}

@Composable
fun SettingsProvider(windowWidthSizeClass: WindowWidthSizeClass, content: @Composable () -> Unit) {
    PreferenceUtil.AppSettingsStateFlow.collectAsState().value.run {
        val tonalPalettes = Color(DEFAULT_SEED_COLOR).toTonalPalettes(PaletteStyle.TonalSpot)

        CompositionLocalProvider(
            LocalDarkTheme provides darkTheme,
            LocalTonalPalettes provides tonalPalettes,
            LocalWindowWidthState provides windowWidthSizeClass,
            content = content,
        )
    }
}

