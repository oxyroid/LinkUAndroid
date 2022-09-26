package com.linku.im.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.linku.domain.bean.ComposeTheme


val LocalTheme = staticCompositionLocalOf<ComposeTheme> { error("no theme provided") }