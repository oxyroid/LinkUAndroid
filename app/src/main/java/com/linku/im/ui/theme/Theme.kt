package com.linku.im.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xff6750a4),
    onPrimary = Color(0xffffffff),
    primaryContainer = Color(0xffeaddff),
    onPrimaryContainer = Color(0xff21005d),
    inversePrimary = Color(0xffd0bcff),
    secondary = Color(0xff625b71),
    onSecondary = Color(0xffffffff),
    secondaryContainer = Color(0xffe8def8),
    onSecondaryContainer = Color(0xff1d192b),
    tertiary = Color(0xff7d5260),
    onTertiary = Color(0xffffffff),
    tertiaryContainer = Color(0xffffd8e4),
    onTertiaryContainer = Color(0xff31111d),
    error = Color(0xffb3261e),
    onError = Color(0xffffffff),
    errorContainer = Color(0xfff9dedc),
    onErrorContainer = Color(0xff410e0b),
    outline = Color(0xff79747e),
    background = Color(0xfffffbfe),
    onBackground = Color(0xff1c1b1f),
    surface = Color(0xffeeeeee),
    onSurface = Color(0xff1c1b1f),
    surfaceVariant = Color(0xffe7e0ec),
    onSurfaceVariant = Color(0xff49454f),
    inverseSurface = Color(0xff313033),
    inverseOnSurface = Color(0xfff4eff4)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xffd0bcff),
    onPrimary = Color(0xff381e72),
    primaryContainer = Color(0xff4f378b),
    onPrimaryContainer = Color(0xffeaddff),
    inversePrimary = Color(0xff6750a4),
    secondary = Color(0xffccc2dc),
    onSecondary = Color(0xff332d41),
    secondaryContainer = Color(0xff4a4458),
    onSecondaryContainer = Color(0xffe8def8),
    tertiary = Color(0xffefb8c8),
    onTertiary = Color(0xff492532),
    tertiaryContainer = Color(0xff633b48),
    onTertiaryContainer = Color(0xffffd8e4),
    error = Color(0xffb3261e),
    onError = Color(0xffffffff),
    errorContainer = Color(0xfff9dedc),
    onErrorContainer = Color(0xff410e0b),
    outline = Color(0xff938f99),
    background = Color(0xff222831),
    onBackground = Color(0xffe6e1e5),
    surface = Color(0xff393e46),
    onSurface = Color(0xffe6e1e5),
    surfaceVariant = Color(0xff49454f),
    onSurfaceVariant = Color(0xffcac4d0),
    inverseSurface = Color(0xffe6e1e5),
    inverseOnSurface = Color(0xff313033)
)

@Composable
fun OssTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    MaterialTheme(
        typography = Typography,
        content = content,
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    )
}
