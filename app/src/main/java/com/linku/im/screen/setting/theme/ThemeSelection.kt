package com.linku.im.screen.setting.theme

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.domain.entity.local.Theme
import com.linku.im.R
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.SugarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelection(
    theme: Theme,
    currentTid: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    fun Int.toColor() = Color(this)
    val selected = currentTid == theme.id
    val alpha by animateFloatAsState(
        if (selected) 0f else 0.4f
    )
    val elevation by animateIntAsState(
        if (selected) 16 else 0
    )

    val zoom by animateFloatAsState(
        if (selected) 1f else 0.8f
    )

    val blurRadius by animateFloatAsState(
        if (selected) 0f else 16f
    )

    @Composable
    fun ColorItem(
        containerColor: Color,
        contentColor: Color,
        left: Boolean
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(
                topStart = LocalSpacing.current.small,
                topEnd = LocalSpacing.current.small,
                bottomStart = if (left) LocalSpacing.current.none else LocalSpacing.current.small,
                bottomEnd = if (!left) LocalSpacing.current.none else LocalSpacing.current.small
            ),
            modifier = Modifier
                .aspectRatio(1f)
                .padding(LocalSpacing.current.extraSmall)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (left) stringResource(id = R.string.theme_card_left)
                    else stringResource(id = R.string.theme_card_right),
                    style = MaterialTheme.typography.bodyLarge
                        .copy(
                            fontSize = if (left) 16.sp
                            else 12.sp
                        ),
                    color = contentColor
                )
            }
        }
    }

    val feedback = LocalHapticFeedback.current
    Box(
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = theme.background.toColor(),
                contentColor = theme.onBackground.toColor()
            ),
            elevation = CardDefaults.outlinedCardElevation(
                defaultElevation = elevation.dp
            ),
            modifier = modifier
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                }
                .aspectRatio(1f)
                .padding(LocalSpacing.current.extraSmall),
            onClick = {
                if (selected) return@OutlinedCard
                feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small),
                modifier = Modifier
                    .graphicsLayer {
                        if (blurRadius != 0f) renderEffect = BlurEffect(blurRadius, blurRadius)
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = Color.Black.copy(
                                alpha = alpha
                            )
                        )
                    }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(LocalSpacing.current.small)
                ) {
                    ColorItem(
                        containerColor = theme.bubbleStart.toColor(),
                        contentColor = theme.onBubbleStart.toColor(),
                        left = true
                    )
                    ColorItem(
                        containerColor = theme.bubbleEnd.toColor(),
                        contentColor = theme.onBubbleEnd.toColor(),
                        left = false
                    )
                }
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.onPrimary.toColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.primary.toColor())
                )
            }
        }

        Crossfade(selected) { selected ->
            if (!selected) {
                Icon(
                    imageVector = when (theme.isDark) {
                        true -> Icons.Rounded.DarkMode
                        false -> Icons.Rounded.LightMode
                    },
                    contentDescription = "",
                    tint = when (theme.isDark) {
                        true -> SugarColors.Tee
                        false -> SugarColors.Yellow
                    }
                )
            }
        }
    }
}