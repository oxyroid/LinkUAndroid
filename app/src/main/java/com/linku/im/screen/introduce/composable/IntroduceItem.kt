package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.linku.im.extension.intervalClickable

private val PADDING_X = 24.dp
private val ENTITY_PADDING_Y = 8.dp
private val FOLDER_PADDING_Y = 16.dp

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
                    .intervalClickable(
                        onClick = onClick
                    )
                    .background(MaterialTheme.colorScheme.background)
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
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = value == null,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = MaterialTheme.colorScheme.onBackground,
                                    )
                                )
                        )
                    }
                    is AnnotatedString? -> {
                        Text(
                            text = value ?: AnnotatedString(""),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = value == null,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = MaterialTheme.colorScheme.onBackground,
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
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
                    .background(MaterialTheme.colorScheme.background)
                    .intervalClickable {

                    }
                    .padding(
                        horizontal = PADDING_X,
                        vertical = FOLDER_PADDING_Y
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                    Icon(
                        imageVector = property.icon,
                        contentDescription = property.icon.name,
                    )
                }
                Text(
                    text = property.label,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = PADDING_X)
                )
            }
        }
    }
}