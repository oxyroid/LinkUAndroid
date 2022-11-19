package com.linku.im.ui.defaults

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.ui.theme.LocalTheme

object ListItemDefault {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun colors(
        enabled: Boolean = true
    ): ListItemColors = run {
        val theme = LocalTheme.current
        val contentAlpha = LocalContentAlpha.current
        val containerColor = remember(theme) { theme.background }
        val contentColor = remember(theme, enabled) {
            if (enabled) {
                theme.onBackground
            } else {
                theme.onBackground * contentAlpha
            }
        }
        val disabledContentColor =
            remember(theme, contentAlpha) { theme.onBackground * contentAlpha }
        ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = contentColor,
            leadingIconColor = contentColor,
            overlineColor = contentColor,
            trailingIconColor = contentColor,
            disabledHeadlineColor = disabledContentColor,
            disabledLeadingIconColor = disabledContentColor,
            disabledTrailingIconColor = disabledContentColor
        )
    }
}
