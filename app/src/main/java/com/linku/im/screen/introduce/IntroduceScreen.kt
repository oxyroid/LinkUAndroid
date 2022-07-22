package com.linku.im.screen.introduce

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.extension.times
import com.linku.im.linku.LinkUEvent
import com.linku.im.screen.introduce.composable.ProfileList
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.ui.ToolBarAction
import com.linku.im.ui.theme.Typography
import com.linku.im.vm
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileScreen(
    viewModel: IntroduceViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var dropdownMenuExpended by remember {
        mutableStateOf(false)
    }

    vm.onActions {
        ToolBarAction(
            onClick = { dropdownMenuExpended = true },
            imageVector = Icons.Default.MoreVert,
            tint = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
        DropdownMenu(
            expanded = dropdownMenuExpended,
            onDismissRequest = { dropdownMenuExpended = false }) {
            DropdownMenuItem(onClick = { viewModel.onEvent(IntroduceEvent.SignOut) }, text = {
                Text(text = stringResource(R.string.sign_out))
            })
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(IntroduceEvent.FetchIntroduce)
    }

    LaunchedEffect(state.logout) {
        if (state.logout) vm.onEvent(LinkUEvent.PopBackStack)
    }

    LaunchedEffect(state.error) {
        state.error.handle { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    state.verifiedEmailStarting.ifTrue {
        AlertDialog(
            onDismissRequest = { },
//            buttons = { Spacer(modifier = Modifier.height(24.dp)) },
            title = {
                CircularProgressIndicator()
            },
            confirmButton = {}
        )
    }

    state.verifiedEmailDialogShowing.ifTrue {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        enabled = !state.verifiedEmailCodeVerifying,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.onEvent(IntroduceEvent.CancelVerifiedEmail) },
                            enabled = !state.verifiedEmailCodeVerifying
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                viewModel.onEvent(IntroduceEvent.VerifiedEmailCode(code))
                                code = ""
                            },
                            enabled = code.isNotBlank() and !state.verifiedEmailCodeVerifying
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (state.verifiedEmailCodeVerifying) R.string.verifying
                                    else R.string.verified
                                )
                            )
                        }
                    }
                }
            }, title = {
                val title = stringResource(id = R.string.email_verified_dialog_title)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }

        )
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            LazyColumn(Modifier.defaultMinSize(minHeight = 1.dp)) {
                item {
                    Text(
                        text = state.actionsLabel,
                        style = Typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(state.actions) {
                    ListItem(text = {
                        Text(
                            text = it.text, color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                        icon = {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = it.text,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                scope.launch {
                                    it.onClick()
                                    sheetState.hide()
                                }
                            })
                }
            }
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    SubcomposeAsyncImage(
                        model = "",
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4 / 3f)
                    )
                }
            }
            item {
                ProfileList(label = stringResource(id = R.string.account),
                    items = state.dataProperties,
                    onItemClick = { setting ->
                        if (setting is Property.Data) {
                            viewModel.onEvent(
                                IntroduceEvent.Actions(
                                    label = setting.key, actions = setting.actions
                                )
                            )
                            scope.launch {
                                if (state.actions.isEmpty()) return@launch
                                sheetState.show()
                            }
                        }
                    })
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outline * 0.1f)
                )
            }
            item {
                ProfileList(label = stringResource(id = R.string.settings),
                    items = state.settingsProperties,
                    onItemClick = {})
            }
        }
    }

    BackHandler(sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
        }
    }
}
