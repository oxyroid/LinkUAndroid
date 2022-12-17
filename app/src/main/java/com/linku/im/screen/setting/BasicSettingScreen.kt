package com.linku.im.screen.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ui.components.MaterialTopBar
import com.linku.im.ui.theme.LocalTheme

@Composable
inline fun BasicSettingScreen(
    title: String,
    crossinline content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalTheme.current
    Scaffold(
        topBar = {
            MaterialTopBar(
                actions = {},
                text = title,
                backgroundColor = theme.topBar.animated(),
                contentColor = theme.onTopBar.animated()
            )
        },
        modifier = modifier,
        backgroundColor = theme.background.animated(),
        contentColor = theme.onBackground.animated()
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            content = content
        )
    }
}
