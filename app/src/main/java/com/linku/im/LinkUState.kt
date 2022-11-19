package com.linku.im

import com.linku.domain.bean.ComposeTheme
import com.linku.domain.bean.midNight
import com.linku.domain.bean.seaSalt

data class LinkUState(
    val loading: Boolean = false,
    val label: String? = null,
    val isDarkMode: Boolean = false,
    val lightTheme: ComposeTheme = seaSalt,
    val darkTheme: ComposeTheme = midNight,
    val isEmojiReady: Boolean = false,
    val isNativeSnackBar: Boolean = true,
    val isThemeReady: Boolean = false,
    val readyForObserveMessages: Boolean = false,
)
