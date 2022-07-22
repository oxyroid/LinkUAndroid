package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.extension.ifTrue
import com.linku.im.extension.times

private val DIVIDER = 0.6.dp
private val PADDING_X = 24.dp
private val ENTITY_PADDING_Y = 8.dp
private val FOLDER_PADDING_Y = 16.dp
private const val TINT_ALPHA = 0.8f

@Composable
fun IntroduceItem(
    property: Property,
    onClick: () -> Unit,
    divider: Boolean = true
) {
    val shimmerColor = MaterialTheme.colorScheme.outline * 0.1f
    when (property) {
        is Property.Data -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        start = PADDING_X, top = ENTITY_PADDING_Y
                    )
            ) {
                when (property.value) {
                    is String -> {
                        Text(
                            text = property.value,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(end = PADDING_X)
                        )
                    }
                    is AnnotatedString -> {
                        Text(
                            text = property.value,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(end = PADDING_X)
                        )
                    }
                    null -> {
                        Text(
                            text = "",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(end = PADDING_X)
                                .placeholder(
                                    visible = true,
                                    color = shimmerColor,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer(
                                        highlightColor = Color.White,
                                    ),
                                )
                        )

                    }
                }

                Spacer(modifier = Modifier.height(ENTITY_PADDING_Y))
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = property.key,
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(
                                end = PADDING_X,
                                bottom = if (divider) 0.dp else ENTITY_PADDING_Y
                            )
                    )
                }
                Spacer(modifier = Modifier.height(ENTITY_PADDING_Y))
            }
            divider.ifTrue {
                Divider(
                    thickness = DIVIDER,
                    color = MaterialTheme.colorScheme.outline * 0.1f
                )
            }
        }
        is Property.Folder -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = {

                    })
                    .padding(
                        start = PADDING_X,
                        top = FOLDER_PADDING_Y
                    )
            ) {
                Icon(
                    imageVector = property.icon,
                    contentDescription = property.icon.name,
                    tint = MaterialTheme.colorScheme.outline * TINT_ALPHA
                )
                Column(
                    modifier = Modifier.padding(start = PADDING_X)
                ) {
                    Text(
                        text = property.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            end = PADDING_X,
                            bottom = if (divider) 0.dp else FOLDER_PADDING_Y
                        )
                    )
                    divider.ifTrue {
                        Divider(
                            thickness = DIVIDER,
                            color = MaterialTheme.colorScheme.outline * 0.1f,
                            modifier = Modifier.padding(top = FOLDER_PADDING_Y, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}