package com.linku.im.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@Composable
fun BottomSheetContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    maxHeight: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var offset by remember {
        mutableStateOf(0f)
    }
    val animateOffset by animateFloatAsState(offset)
    var pressed by remember {
        mutableStateOf(false)
    }
    val state = rememberDraggableState {
        offset += it
    }
    val theme = LocalTheme.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Surface(
            color = theme.background,
            contentColor = theme.onBackground,
            shape = RoundedCornerShape(
                topStart = LocalSpacing.current.medium,
                topEnd = LocalSpacing.current.medium
            ),
            modifier = modifier
                .graphicsLayer {
                    translationY = animateOffset.coerceAtLeast(0f)
                }
        ) {
            val configuration = LocalConfiguration.current
            val feedback = LocalHapticFeedback.current
            val density = LocalDensity.current.density
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (maxHeight) it.fillMaxHeight()
                        else it.wrapContentHeight()
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = state,
                        onDragStarted = {
                            feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            pressed = true
                        },
                        onDragStopped = {
                            if (offset > configuration.screenHeightDp * density / 4) {
                                onDismiss()
                            }
                            offset = 0f
                            pressed = false
                        },
                    )
                    .padding(LocalSpacing.current.medium),
            ) {
                val dividerColor by animateColorAsState(
                    if (pressed) theme.primary * 0.45f else theme.topBar
                )
                Divider(
                    color = dividerColor,
                    thickness = LocalSpacing.current.small,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(64.dp)
                        .clip(CircleShape)
                )
                content()
            }
        }
    }
}
