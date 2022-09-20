package com.linku.im.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.domain.entity.Member
import com.linku.im.R
import com.linku.im.extension.intervalClickable
import com.linku.im.extension.times
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@Composable
fun MemberItem(
    modifier: Modifier = Modifier,
    member: Member? = null,
    avatar: String?,
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
                enabled = member != null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleHeadPicture(
            model = avatar,
            placeholder = {
                TextImage(
                    text = member?.name ?: "",
                )
            },
            modifier = Modifier.padding(
                horizontal = LocalSpacing.current.medium,
                vertical = LocalSpacing.current.extraSmall
            )
        )
        Column(
            modifier = Modifier
                .padding(
                    end = LocalSpacing.current.medium,
                    top = LocalSpacing.current.small,
                )
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = member?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = LocalTheme.current.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = member == null,
                        color = shimmerColor,
                        shape = RoundedCornerShape(4.dp),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = onShimmerColor,
                            animationSpec = shimmerAnimationSpec
                        )
                    ),
                overflow = TextOverflow.Ellipsis
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = when {
                        member?.root == true -> stringResource(R.string.channel_member_root)
                        else -> ""
                    },
                    color = when {
                        member?.root == true -> LocalTheme.current.primary
                        else -> Color.Unspecified
                    },
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .placeholder(
                            visible = (member == null),
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
            Divider(
                thickness = 1.dp,
                color = LocalTheme.current.divider
            )
        }
    }
}
