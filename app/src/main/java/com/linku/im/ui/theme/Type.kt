package com.linku.im.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.linku.im.R

object AppFont {
    val Default = FontFamily(
        Font(R.font.titillium_web_regular),
        Font(R.font.titillium_web_italic, style = FontStyle.Italic),
        Font(R.font.titillium_web_medium, FontWeight.Medium),
        Font(R.font.titillium_web_medium_italic, FontWeight.Medium, style = FontStyle.Italic),
        Font(R.font.titillium_web_bold, FontWeight.Bold),
        Font(R.font.titillium_web_bold_italic, FontWeight.Bold, style = FontStyle.Italic),
    )
}

fun TextStyle.withDefaultFontFamily() = copy(
    fontFamily = AppFont.Default
)
