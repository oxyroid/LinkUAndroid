package com.linku.im.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomTheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryDisable: Color,
    val onPrimaryDisable: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val pressed: Color,
    val onPressed: Color,
    val chatBackground: Color,
    val bubbleEnd: Color,
    val onBubbleEnd: Color,
    val bubbleStart: Color,
    val onBubbleStart: Color,
    val divider: Color,
    val error: Color
)

val defaultLight = CustomTheme(
    primary = Color(0xff837fc9),
    surface = Color(0xFFeeeeee),
    onSurface = Color(0xFF191C1B),
    chatBackground = Color(0xff7eb2a8),
    bubbleStart = Color(0xffefefef),
    onBubbleStart = Color(0xff000000),
    background = Color(0xfffefefe),
    onBackground = Color(0xff2a2a2a),
    pressed = Color(0xfff8f8f8),
    onPressed = Color(0xff323232),
    primaryDisable = Color(0xffc7c6cb),
    onPrimaryDisable = Color(0xfff6f5f9),
    onPrimary = Color(0xffeef7fb),
    bubbleEnd = Color(0xff5a91de),
    onBubbleEnd = Color(0xffdcf7fa),
    divider = Color(0xffefefef),
    error = Color(0xFFBA1A1A)
)

val LocalTheme = staticCompositionLocalOf<CustomTheme> { error("no theme provided") }