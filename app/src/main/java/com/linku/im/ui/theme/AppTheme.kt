package com.linku.im.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.linku.im.vm

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val state = vm.readable
    val colors = remember(useDarkTheme, state.lightTheme, state.darkTheme) {
        if (useDarkTheme) state.darkTheme else state.lightTheme
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalTheme provides colors,
        LocalDuration provides Duration()
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
