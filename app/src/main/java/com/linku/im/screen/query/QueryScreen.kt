package com.linku.im.screen.query

import android.widget.Toast
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.linku.LinkUEvent
import com.linku.im.screen.Screen
import com.linku.im.screen.query.composable.QueryItem
import com.linku.im.vm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueryScreen(
    viewModel: QueryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state
    vm.onActions {

    }
    vm.onTitle {
        Row {
            OutlinedTextField(
                value = state.text,
                onValueChange = { viewModel.onEvent(QueryEvent.OnText(it)) },
                Modifier
                    .weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.query_input),
                        color = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary,
                    textColor = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.onEvent(QueryEvent.Query) }
                )
            )
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
    ) {
        stickyHeader {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp)
            ) {
                ElevatedFilterChip(
                    selected = state.isDescription,
                    onClick = { viewModel.onEvent(QueryEvent.ToggleIncludeDescription) },
                    label = {
                        Text(
                            text = stringResource(id = R.string.query_filter_description),
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = ""
                        )
                    }
                )
            }

        }
        items(state.conversations) { conversation ->
            QueryItem(conversation = conversation) {
                vm.onEvent(LinkUEvent.NavigateWithArgs(Screen.ChatScreen.withArgs(conversation.id)))
            }
        }
    }
    LaunchedEffect(state.message) {
        state.message.handle {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}