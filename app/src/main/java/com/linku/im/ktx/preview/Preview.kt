@file:Suppress("unused")

package com.linku.im.ktx.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.linku.domain.bean.midNight
import com.linku.domain.bean.seaSalt
import com.linku.im.ui.theme.LocalTheme

@Composable
fun PreviewContainer(
    useDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTheme provides if (useDarkMode) midNight else seaSalt
    ) {
        content()
    }
}
