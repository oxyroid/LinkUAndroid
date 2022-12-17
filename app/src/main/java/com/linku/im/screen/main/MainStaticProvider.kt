package com.linku.im.screen.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.linku.im.R
import com.linku.im.nav.target.NavTarget

internal object MainStaticProvider {
    @Composable
    fun provideSelections(
        isDarkMode: Boolean,
        onNotification: () -> Unit,
        onTheme: () -> Unit,
        onGift: () -> Unit,
        onToggleDarkMode: () -> Unit
    ): List<Selection> = remember(isDarkMode) {
        listOf(
            Selection.Button(
                resId = R.string.notification,
                icon = Icons.Rounded.Notifications,
                onClick = onNotification
            ),
            Selection.Route(
                resId = R.string.settings,
                target = NavTarget.Introduce(-1),
                icon = Icons.Rounded.Settings
            ),
            Selection.Switch(
                resId = R.string.toggle_theme,
                value = isDarkMode,
                onIcon = Icons.Rounded.LightMode,
                offIcon = Icons.Rounded.DarkMode,
                onClick = onToggleDarkMode,
                onLongClick = onTheme
            ),
            Selection.Button(
                resId = R.string.gift,
                icon = Icons.Rounded.CardGiftcard,
                onClick = onGift
            )
        )
    }

    @Composable
    fun provideTabLabels(
        isQuery: Boolean
    ): List<String> = if (isQuery) {
        listOf(
            stringResource(R.string.query_result_message),
            stringResource(R.string.query_result_conversation),
            stringResource(R.string.query_result_user)
        )
    } else {
        listOf(
            stringResource(R.string.tab_main),
            stringResource(R.string.tab_notification),
            stringResource(R.string.tab_contact),
            stringResource(R.string.tab_more)
        )
    }
}