package com.linku.domain.entity.local

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.linku.domain.bean.ComposeTheme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
@Stable
data class Theme(
    @SerialName("d")
    val isDark: Boolean,
    @SerialName("dt")
    val isDarkText: Boolean,
    @SerialName("p")
    val primary: Int,
    @SerialName("op")
    val onPrimary: Int,
    @SerialName("pd")
    val primaryDisable: Int,
    @SerialName("opd")
    val onPrimaryDisable: Int,
    @SerialName("s")
    val surface: Int,
    @SerialName("os")
    val onSurface: Int,
    @SerialName("tb")
    val topBar: Int,
    @SerialName("otb")
    val onTopBar: Int,
    @SerialName("stb")
    val secondaryTopBar: Int,
    @SerialName("ostb")
    val onSecondaryTopBar: Int,
    @SerialName("b")
    val background: Int,
    @SerialName("ob")
    val onBackground: Int,
    @SerialName("pr")
    val pressed: Int,
    @SerialName("opr")
    val onPressed: Int,
    @SerialName("cb")
    val chatBackground: Int,
    @SerialName("be")
    val bubbleEnd: Int,
    @SerialName("obe")
    val onBubbleEnd: Int,
    @SerialName("bs")
    val bubbleStart: Int,
    @SerialName("obs")
    val onBubbleStart: Int,
    @SerialName("dr")
    val divider: Int,
    @SerialName("e")
    val error: Int,
    @SerialName("oe")
    val onError: Int,
    @SerialName("name")
    val name: String,
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0
) {
    companion object {
        const val NOT_EXIST_ID = -1
    }
}

fun Theme.toComposeTheme(): ComposeTheme = ComposeTheme(
    isDark = isDark,
    isDarkText = isDarkText,
    name = name,
    primary = primary.toColor(),
    onPrimary = onPrimary.toColor(),
    primaryDisable = primaryDisable.toColor(),
    onPrimaryDisable = onPrimaryDisable.toColor(),
    surface = surface.toColor(),
    onSurface = onSurface.toColor(),
    topBar = topBar.toColor(),
    onTopBar = onTopBar.toColor(),
    secondaryTopBar = secondaryTopBar.toColor(),
    onSecondaryTopBar = onSecondaryTopBar.toColor(),
    background = background.toColor(),
    onBackground = onBackground.toColor(),
    pressed = pressed.toColor(),
    onPressed = onPressed.toColor(),
    chatBackground = chatBackground.toColor(),
    bubbleEnd = bubbleEnd.toColor(),
    onBubbleEnd = onBubbleEnd.toColor(),
    bubbleStart = bubbleStart.toColor(),
    onBubbleStart = onBubbleStart.toColor(),
    divider = divider.toColor(),
    error = error.toColor(),
    onError = onError.toColor(),
    id = id
)


fun ComposeTheme.toTheme(): Theme = Theme(
    isDark = isDark,
    isDarkText = isDarkText,
    name = name,
    primary = primary.toInt(),
    onPrimary = onPrimary.toInt(),
    primaryDisable = primaryDisable.toInt(),
    onPrimaryDisable = onPrimaryDisable.toInt(),
    surface = surface.toInt(),
    onSurface = onSurface.toInt(),
    topBar = topBar.toInt(),
    onTopBar = onTopBar.toInt(),
    secondaryTopBar = secondaryTopBar.toInt(),
    onSecondaryTopBar = onSecondaryTopBar.toInt(),
    background = background.toInt(),
    onBackground = onBackground.toInt(),
    pressed = pressed.toInt(),
    onPressed = onPressed.toInt(),
    chatBackground = chatBackground.toInt(),
    bubbleEnd = bubbleEnd.toInt(),
    onBubbleEnd = onBubbleEnd.toInt(),
    bubbleStart = bubbleStart.toInt(),
    onBubbleStart = onBubbleStart.toInt(),
    divider = divider.toInt(),
    error = error.toInt(),
    onError = onError.toInt()
)

@Stable
internal fun Int.toColor(): Color = Color(this)

internal fun Color.toInt(): Int = android.graphics.Color.argb(alpha, red, green, blue)
