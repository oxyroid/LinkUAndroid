package com.linku.im.screen.chat.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.linku.domain.struct.LinkedNode
import com.linku.im.screen.chat.ChatScreenMode
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@Composable
fun ChatTopBar(
    label: String,
    node: LinkedNode<ChatScreenMode>,
    onClick: (ChatScreenMode) -> Unit,
    onNavClick: (ChatScreenMode) -> Unit
) {
    val duration = 400
    val containerColor by animateColorAsState(
        if (node.value == ChatScreenMode.ChannelDetail) LocalTheme.current.primary
        else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (node.value == ChatScreenMode.ChannelDetail) LocalTheme.current.onPrimary
        else LocalContentColor.current
    )
    Column(
        modifier = Modifier
            .drawWithContent {
                drawRect(containerColor)
                drawContent()
            }
            .clickable { onClick(node.value) }
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = LocalSpacing.current.small)
        ) {
            val (icon, text, subText) = createRefs()
            MaterialIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = { onNavClick(node.value) },
                tint = contentColor,
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            )
            val textHorizontallyBias by animateFloatAsState(
                when (node.value) {
                    ChatScreenMode.Messages -> 0.5f
                    ChatScreenMode.ChannelDetail -> 0f
                    else -> 0.3f
                },
                animationSpec = tween(duration, easing = LinearEasing)
            )
            val textVerticallyBias by animateFloatAsState(
                when (node.value) {
                    ChatScreenMode.Messages -> 0.5f
                    ChatScreenMode.ChannelDetail -> 1f
                    else -> 0.7f
                },
                animationSpec = tween(duration, easing = LinearEasing)
            )
            val fontSize by animateFloatAsState(
                if (node.value == ChatScreenMode.Messages) 16f
                else 24f,
                animationSpec = tween(duration, easing = LinearEasing)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontSize = fontSize.sp,
                modifier = Modifier
                    .padding(
                        horizontal = LocalSpacing.current.medium,
                        vertical = LocalSpacing.current.extraSmall
                    )
                    .constrainAs(text) {
                        centerHorizontallyTo(parent, textHorizontallyBias)
                        centerVerticallyTo(parent, textVerticallyBias)
                    }
            )

            val subTextHeight by animateDpAsState(
                when (node.value) {
                    ChatScreenMode.ChannelDetail -> 120.dp
                    else -> 0.dp
                },
                animationSpec = tween(duration, easing = LinearEasing)
            )
            Text(
                text = "",
                modifier = Modifier
                    .constrainAs(subText) {
                        top.linkTo(icon.bottom)
                        bottom.linkTo(parent.bottom)
                        centerHorizontallyTo(parent)
                    }
                    .fillMaxWidth()
                    .height(subTextHeight)
            )
        }
    }
}