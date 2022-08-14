package com.linku.im.screen.query

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.Screen
import com.linku.im.screen.query.composable.QueryItem
import com.linku.im.ui.components.TextInputFieldOne
import com.linku.im.vm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueryScreen(
    viewModel: QueryViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
        vm.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalContentColor provides if (vm.readable.isDarkMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(if (vm.readable.isDarkMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { vm.onEvent(LinkUEvent.PopBackStack) },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextInputFieldOne(
                        textFieldValue = state.text,
                        onValueChange = { viewModel.onEvent(QueryEvent.OnText(it)) },
                        imeAction = ImeAction.Search,
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.onEvent(QueryEvent.Query)
                            }
                        ),
                        modifier = Modifier.padding(8.dp)
                    )

                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.background,
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
                                }
                            )
                        }

                    }
                    items(state.conversations) { conversation ->
                        QueryItem(conversation = conversation) {
                            vm.onEvent(
                                LinkUEvent.NavigateWithArgs(
                                    Screen.ChatScreen.withArgs(
                                        conversation.id
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}