package com.wzk.oss.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xff00658a),
    onPrimary = Color(0xffffffff),
    primaryContainer = Color(0xffc4e7ff),
    onPrimaryContainer = Color(0xff001e2c),
    secondary = Color(0xff4e616d),
    onSecondary = Color(0xffffffff),
    secondaryContainer = Color(0xffd1e5f4),
    onSecondaryContainer = Color(0xff0a1e28),
    tertiary = Color(0xff615a7d),
    onTertiary = Color(0xffffffff),
    tertiaryContainer = Color(0xffe7deff),
    onTertiaryContainer = Color(0xff1d1736),
    error = Color(0xffba1a1a),
    onError = Color(0xffffffff),
    errorContainer = Color(0xffffdad6),
    onErrorContainer = Color(0xff410002),
    background = Color(0xfffbfcff),
    onBackground = Color(0xff191c1e),
    surface = Color(0xfffbfcff),
    onSurface = Color(0xff191c1e),
    outline = Color(0xff71787e),
    surfaceVariant = Color(0xffdde3ea),
    onSurfaceVariant = Color(0xff41484d),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xff7cd0ff),
    onPrimary = Color(0xff00344a),
    primaryContainer = Color(0xff004c69),
    onPrimaryContainer = Color(0xffc4e7ff),
    secondary = Color(0xffb5c9d7),
    onSecondary = Color(0xff20333e),
    secondaryContainer = Color(0xff374955),
    onSecondaryContainer = Color(0xffd1e5f4),
    tertiary = Color(0xffcac1e9),
    onTertiary = Color(0xff322c4c),
    tertiaryContainer = Color(0xff494264),
    onTertiaryContainer = Color(0xffe7deff),
    error = Color(0xffffb4ab),
    onError = Color(0xff690005),
    errorContainer = Color(0xff93000a),
    onErrorContainer = Color(0xffffdad6),
    background = Color(0xff191c1e),
    onBackground = Color(0xffe1e2e5),
    surface = Color(0xff2b2b2b),
    onSurface = Color(0xffe1e2e5),
    outline = Color(0xff8b9297),
    surfaceVariant = Color(0xff41484d),
    onSurfaceVariant = Color(0xffc0c7cd)
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
