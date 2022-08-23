package com.linku.im.screen.main.composable

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.extension.ifTrue
import com.linku.im.extension.intervalClickable
import com.linku.im.extension.times
import com.linku.im.screen.main.MainState
import com.linku.im.ui.components.TextImage
import com.linku.im.ui.theme.LocalTheme

@Composable
fun ConversationItem(
    modifier: Modifier = Modifier,
    conversation: MainState.ConversationMainUI? = null,
    unreadCount: Int = 0,
    pinned: Boolean = false,
    onClick: () -> Unit = {}
) {
    val shimmerColor = LocalTheme.current.divider * 0.3f
    val onShimmerColor = Color.White
    val shimmerAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                delayMillis = 400
            ),
            repeatMode = RepeatMode.Restart
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .let {
                pinned.ifTrue {
                    it.background(LocalTheme.current.surface * 0.8f)
                } ?: it
            }
            .intervalClickable(
                enabled = conversation != null,
                onClick = onClick
            )
            .padding(
                horizontal = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleHeadPicture(
            model = conversation?.image,
            name = conversation?.name,
            placeholder = { TextImage(text = conversation?.name ?: "") }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .padding(
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conversation?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = LocalTheme.current.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = conversation == null,
                        color = shimmerColor,
                        shape = RoundedCornerShape(4.dp),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = onShimmerColor,
                            animationSpec = shimmerAnimationSpec,
                        )
                    ),
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            CompositionLocalProvider(LocalContentColor provides LocalTheme.current.onSurface * 0.8f) {
                Text(
                    text = conversation?.content ?: "",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .placeholder(
                            visible = (conversation == null),
                            color = shimmerColor,
                            shape = RoundedCornerShape(4.dp),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = onShimmerColor,
                                animationSpec = shimmerAnimationSpec
                            ),
                        )
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
        Spacer(modifier = Modifier.width(8.dp))
        when {
            (unreadCount != 0) -> {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = LocalTheme.current.primary
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = LocalTheme.current.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            pinned -> {
                Surface(
                    shape = CircleShape,
                    color = LocalTheme.current.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Lock,
                        contentDescription = "",
                        tint = LocalTheme.current.onPrimary,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleHeadPicture(
    model: Any?,
    name: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (String?) -> Unit = {}
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = name,
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(100)),
        error = {
            placeholder(name)
        },
        loading = {
            placeholder(name)
        }
    )

}

