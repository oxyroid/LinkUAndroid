package com.linku.im.ui.components.notify

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import kotlinx.coroutines.delay

@Composable
internal fun NotifyTextHolder(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Warning,
    delay: Long = 800L,
    animationSpec: AnimationSpec<Float> = spring()
) {
    var iconVisibility by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (iconVisibility) 1f else 0f,
        animationSpec = animationSpec
    )
    LaunchedEffect(Unit) {
        delay(delay)
        iconVisibility = true
    }

    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.disabled
    ) {
        Column(
            modifier = Modifier.background(LocalTheme.current.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = LocalSpacing.current.medium
                    )
                    .height(LocalSpacing.current.largest)
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                    tint = LocalTheme.current.onSurface * 0.6f
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall
                        .copy(
                            baselineShift = BaselineShift.None
                        ),
                    color = LocalTheme.current.onSurface * 0.6f,
                    modifier = modifier.weight(1f)
                )
            }
            Box(modifier = Modifier.navigationBarsPadding())
        }
    }
}
