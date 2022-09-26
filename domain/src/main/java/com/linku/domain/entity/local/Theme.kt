package com.linku.domain.entity.local

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.linku.domain.bean.ComposeTheme
import kotlinx.serialization.Serializable

@Entity
@Serializable
@Stable
@Keep
data class Theme(
    val isDark: Boolean,
    val primary: Int,
    val onPrimary: Int,
    val primaryDisable: Int,
    val onPrimaryDisable: Int,
    val surface: Int,
    val onSurface: Int,
    val secondarySurface: Int,
    val onSecondarySurface: Int,
    val background: Int,
    val onBackground: Int,
    val pressed: Int,
    val onPressed: Int,
    val chatBackground: Int,
    val bubbleEnd: Int,
    val onBubbleEnd: Int,
    val bubbleStart: Int,
    val onBubbleStart: Int,
    val divider: Int,
    val error: Int,
    val onError: Int,
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    companion object {
        const val NOT_EXIST_ID = -1
    }
}

fun Theme.toComposeTheme(): ComposeTheme = ComposeTheme(
    isDark = isDark,
    name = name,
    primary = primary.toColor(),
    onPrimary = onPrimary.toColor(),
    primaryDisable = primaryDisable.toColor(),
    onPrimaryDisable = onPrimaryDisable.toColor(),
    surface = surface.toColor(),
    onSurface = onSurface.toColor(),
    secondarySurface = secondarySurface.toColor(),
    onSecondarySurface = onSecondarySurface.toColor(),
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
    name = name,
    primary = primary.toInt(),
    onPrimary = onPrimary.toInt(),
    primaryDisable = primaryDisable.toInt(),
    onPrimaryDisable = onPrimaryDisable.toInt(),
    surface = surface.toInt(),
    onSurface = onSurface.toInt(),
    secondarySurface = secondarySurface.toInt(),
    onSecondarySurface = onSecondarySurface.toInt(),
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

internal fun Int.toColor(): Color = Color(this)

internal fun Color.toInt(): Int = android.graphics.Color.argb(alpha, red, green, blue)
