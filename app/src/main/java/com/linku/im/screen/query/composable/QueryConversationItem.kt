package com.linku.im.screen.query.composable

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.domain.entity.Conversation
import com.linku.im.extension.intervalClickable
import com.linku.im.extension.times
import com.linku.im.ui.components.TextImage
import com.linku.im.ui.theme.LocalTheme

@Composable
fun QueryConversationItem(
    modifier: Modifier = Modifier,
    conversation: Conversation? = null,
    onClick: () -> Unit = {}
) {
    val shimmerColor = LocalTheme.current.onSurface * 0.8f
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
            .intervalClickable(
                enabled = (conversation != null),
                onClick = onClick
            )
            .padding(
                horizontal = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleHeadPicture(
            model = conversation?.avatar,
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
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = conversation?.description ?: "",
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
    }
}

@Composable
private fun CircleHeadPicture(
    model: Any?,
    name: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (String?) -> Unit = {}
) {
    Card(
        shape = CircleShape,
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxHeight()
            .aspectRatio(1f)
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = name,
            modifier = Modifier
                .fillMaxSize(),
            error = {
                placeholder(name)
            },
            loading = {
                placeholder(name)
            }
        )
    }
}

