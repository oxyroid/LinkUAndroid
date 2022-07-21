package com.linku.im.screen.chat.composable

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.linku.im.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatBottomBar(
    text: String,
    firstVisibleItemIndex: Int,
    listState: LazyListState,
    onAction: () -> Unit,
    onSend: () -> Unit,
    onText: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val elevation by animateDpAsState(targetValue = if (listState.isScrollInProgress) 16.dp else 0.dp)
    Surface(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface,
        elevation = elevation
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            AnimatedVisibility(
                visible = firstVisibleItemIndex != 0,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                            onAction()
                        }
                    },
                    shape = RoundedCornerShape(30),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = (-8).dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = (-4).dp
                    )
                ) {
                    Text(
                        text = firstVisibleItemIndex.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    onText(it)
                },
                Modifier
                    .weight(1f)
                    .animateContentSize { initialValue, targetValue -> },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.screen_chat_input),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            AnimatedVisibility(visible = text.isNotBlank()) {
                IconButton(
                    onClick = { onSend() },
                ) {
                    val imageVector = Icons.Rounded.Send
                    Icon(
                        imageVector = imageVector,
                        contentDescription = "send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}