package com.linku.im.screen.main.composable

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.domain.entity.Conversation
import com.linku.im.extension.times

@Composable
fun MainConversationItem(
    conversation: Conversation? = null,
    unreadCount: Int = 0,
    pinned: Boolean = false,
    onClick: () -> Unit = {}
) {
    val shimmerColor = MaterialTheme.colorScheme.outline * 0.3f
    val onShimmerColor = Color.White
    val shimmerAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 400),
            repeatMode = RepeatMode.Restart
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .let {
                if (pinned)
                    it.background(
                        color = MaterialTheme.colorScheme.surface
                    )
                else it

            }
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(100),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .aspectRatio(1f)
        ) {
            SubcomposeAsyncImage(
                model = conversation?.avatar ?: "",
                contentDescription = conversation?.name ?: "",
                modifier = Modifier
                    .fillMaxSize(),
                error = {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conversation?.name ?: "",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
            Text(
                text = conversation?.description ?: "",
                color = MaterialTheme.colorScheme.onSurface * 0.8f,
                maxLines = 1,
                modifier = Modifier
                    .placeholder(
                        visible = conversation == null,
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
        Spacer(modifier = Modifier.width(8.dp))
        if (unreadCount != 0) {
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (pinned) {
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}