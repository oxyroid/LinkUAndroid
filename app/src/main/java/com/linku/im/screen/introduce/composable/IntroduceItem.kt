package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.extension.ifTrue
import com.linku.im.ui.theme.divider
import com.linku.im.vm

private val DIVIDER = 0.6.dp
private val PADDING_X = 24.dp
private val ENTITY_PADDING_Y = 8.dp
private val FOLDER_PADDING_Y = 16.dp

@Composable
fun IntroduceItem(
    property: Property,
    onClick: () -> Unit,
    divider: Boolean = true
) {
    val state = vm.readable
    val darkMode = state.isDarkMode
    when (property) {
        is Property.Data -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onClick,
                        role = Role.Tab
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        start = PADDING_X,
                        top = ENTITY_PADDING_Y
                    )
            ) {
                when (val value = property.value) {
                    is String? -> {
                        Text(
                            text = value ?: "",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(end = PADDING_X)
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
                                .padding(end = PADDING_X)
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

                Spacer(modifier = Modifier.height(ENTITY_PADDING_Y))
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                    Text(
                        text = property.key,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(
                                end = PADDING_X,
                                bottom = if (divider) 0.dp else ENTITY_PADDING_Y
                            )
                    )
                }
                Spacer(Modifier.height(ENTITY_PADDING_Y))
            }
            divider.ifTrue {
                Divider(
                    thickness = DIVIDER,
                    color = MaterialTheme.colorScheme.divider(darkMode)
                )
            }
        }
        is Property.Folder -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(
                        onClick = {

                        },
                        role = Role.Tab
                    )
                    .padding(
                        start = PADDING_X,
                        top = FOLDER_PADDING_Y
                    )
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                    Icon(
                        imageVector = property.icon,
                        contentDescription = property.icon.name
                    )
                }
                Column(
                    modifier = Modifier.padding(start = PADDING_X)
                ) {
                    Text(
                        text = property.label,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            end = PADDING_X,
                            bottom = if (divider) 0.dp else FOLDER_PADDING_Y
                        )
                    )

                    divider.ifTrue {
                        Divider(
                            thickness = DIVIDER,
                            color = MaterialTheme.colorScheme.divider(darkMode),
                            modifier = Modifier.padding(top = FOLDER_PADDING_Y, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}