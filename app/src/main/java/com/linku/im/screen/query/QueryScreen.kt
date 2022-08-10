package com.linku.im.screen.query

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.Screen
import com.linku.im.screen.query.composable.QueryItem
import com.linku.im.vm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueryScreen(
    viewModel: QueryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row {
                IconButton(
                    onClick = { vm.onEvent(LinkUEvent.PopBackStack) },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    textStyle = MaterialTheme.typography.titleMedium,
                    value = state.text,
                    onValueChange = { viewModel.onEvent(QueryEvent.OnText(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        textColor = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.onEvent(QueryEvent.Query)
                        }
                    )
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