package com.linku.im.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import com.linku.im.R

fun TextStyle.withMedium() = copy(
    fontFamily = Fonts.Medium
)

fun TextStyle.withItalic() = copy(
    fontFamily = Fonts.Italic
)

fun TextStyle.withMono() = copy(
    fontFamily = Fonts.Mono
)

fun TextStyle.withMediumItalic() = copy(
    fontFamily = Fonts.MediumItalic
)

fun TextStyle.withBold() = copy(
    fontFamily = Fonts.Bold
)

fun TextStyle.withBlack() = copy(
    fontFamily = Fonts.Black
)

object Fonts {
    val Medium = Font(R.font.rmedium).toFontFamily()
    val Italic = Font(R.font.ritalic).toFontFamily()
    val Mono = Font(R.font.rmono).toFontFamily()
    val MediumItalic = Font(R.font.rmediumitalic).toFontFamily()
    val Bold = Font(R.font.mw_bold).toFontFamily()
    val Black = Font(R.font.rcondensedbold).toFontFamily()
}

val TitleFontSize = 16.sp
val SubTitleFontSize = 14.sp