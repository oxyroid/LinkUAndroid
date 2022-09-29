package com.linku.im.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.R
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ktx.compose.ui.intervalClickable
import com.linku.im.screen.chat.vo.MemberVO
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@Composable
fun MemberItem(
    modifier: Modifier = Modifier,
    member: MemberVO? = null,
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
        val display = remember(member) {
            member?.memberName?.ifEmpty { member.username }.orEmpty()
        }
        CircleHeadPicture(
            model = member?.avatar,
            placeholder = {
                TextImage(display)
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
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = member?.username.orEmpty(),
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
                        member == null -> ""
                        member.admin -> stringResource(R.string.channel_member_root)
                        else -> member.memberName
                    },
                    color = when {
                        member == null -> Color.Unspecified
                        member.admin -> LocalTheme.current.primary
                        member.memberName.isNotEmpty() -> LocalTheme.current.primary
                        else -> Color.Unspecified
                    },
                    maxLines = 1,
                    style = when {
                        member == null -> MaterialTheme.typography.bodyMedium
                        member.admin -> MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black
                        )

                        else -> MaterialTheme.typography.bodyMedium
                    },
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
        }
    }
}
