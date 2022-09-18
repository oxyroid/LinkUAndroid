package com.linku.im.ui.theme

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.navigation.findNavController
import com.linku.im.vm


@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) defaultDark else defaultLight

    val containerColor by animateColorAsState(
        if (vm.readable.isDarkMode) colors.surface
        else colors.primary
    )
    val onContainerColor by animateColorAsState(
        if (vm.readable.isDarkMode) colors.onSurface
        else colors.onPrimary
    )

    val backgroundColor by animateColorAsState(colors.background)
    val onBackgroundColor by animateColorAsState(colors.onBackground)
    val surfaceColor by animateColorAsState(colors.surface)
    val onSurfaceColor by animateColorAsState(colors.onSurface)

    val animatedColor by remember {
        derivedStateOf {
            AnimatedColor(
                containerColor = containerColor,
                onContainerColor = onContainerColor,
                backgroundColor = backgroundColor,
                onBackgroundColor = onBackgroundColor,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor
            )
        }
    }
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalNavController provides LocalView.current.findNavController(),
        LocalAnimatedColor provides animatedColor,
        LocalTheme provides colors
    ) {
        MaterialTheme(
            content = content,
            typography = MaterialTheme.typography.copy(
                titleLarge = MaterialTheme.typography.titleLarge.withDefaultFontFamily(),
                titleMedium = MaterialTheme.typography.titleMedium.withDefaultFontFamily(),
                titleSmall = MaterialTheme.typography.titleSmall.withDefaultFontFamily(),
                bodyLarge = MaterialTheme.typography.bodyLarge.withDefaultFontFamily(),
                bodyMedium = MaterialTheme.typography.bodyMedium.withDefaultFontFamily(),
                bodySmall = MaterialTheme.typography.bodySmall.withDefaultFontFamily(),
                displayLarge = MaterialTheme.typography.displayLarge.withDefaultFontFamily(),
                displayMedium = MaterialTheme.typography.displayMedium.withDefaultFontFamily(),
                displaySmall = MaterialTheme.typography.displaySmall.withDefaultFontFamily(),
                headlineLarge = MaterialTheme.typography.headlineLarge.withDefaultFontFamily(),
                headlineMedium = MaterialTheme.typography.headlineMedium.withDefaultFontFamily(),
                headlineSmall = MaterialTheme.typography.headlineSmall.withDefaultFontFamily(),
                labelLarge = MaterialTheme.typography.labelLarge.withDefaultFontFamily(),
                labelMedium = MaterialTheme.typography.labelMedium.withDefaultFontFamily(),
                labelSmall = MaterialTheme.typography.labelSmall.withDefaultFontFamily()
            )
        )
    }
}
