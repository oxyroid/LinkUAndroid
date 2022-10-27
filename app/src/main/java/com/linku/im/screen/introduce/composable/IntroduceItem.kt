package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ktx.compose.ui.intervalClickable
import com.linku.im.ui.theme.LocalTheme

private val PADDING_X = 24.dp
private val ENTITY_PADDING_Y = 8.dp
private val FOLDER_PADDING_Y = 12.dp

@Composable
fun IntroduceItem(
    property: Property,
    onClick: () -> Unit
) {
    when (property) {
        is Property.Data -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .intervalClickable(onClick = onClick)
                    .background(LocalTheme.current.background)
                    .padding(
                        horizontal = PADDING_X,
                        vertical = ENTITY_PADDING_Y
                    ),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                when (val value = property.value) {
                    is String? -> {
                        Text(
                            text = value ?: "",
                            color = LocalTheme.current.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = value == null,
                                    color = LocalTheme.current.onBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = LocalTheme.current.onBackground,
                                    )
                                )
                        )
                    }

                    is AnnotatedString? -> {
                        Text(
                            text = value ?: AnnotatedString(""),
                            color = LocalTheme.current.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = value == null,
                                    color = LocalTheme.current.onBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = LocalTheme.current.onBackground,
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                CompositionLocalProvider(LocalContentColor provides LocalTheme.current.onBackground * 0.8f) {
                    Text(
                        text = property.key,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is Property.Folder -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalTheme.current.background)
                    .intervalClickable(onClick = onClick)
                    .padding(
                        horizontal = PADDING_X,
                        vertical = FOLDER_PADDING_Y
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides LocalTheme.current.onBackground * 0.8f) {
                    Icon(
                        imageVector = property.icon,
                        contentDescription = property.icon.name,
                    )
                }
                Text(
                    text = property.label,
                    color = LocalTheme.current.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = PADDING_X)
                )
            }
        }
    }
}
