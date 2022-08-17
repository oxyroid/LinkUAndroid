package com.linku.im.screen.introduce

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Verified
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.im.BuildConfig
import com.linku.im.R
import com.linku.im.extension.ifFalse
import com.linku.im.extension.ifTrue
import com.linku.im.extension.intervalClickable
import com.linku.im.screen.Screen
import com.linku.im.screen.introduce.composable.ProfileList
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalExpandColor
import com.linku.im.ui.theme.LocalNavController
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun IntroduceScreen(
    uid: Int, viewModel: IntroduceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val navController = LocalNavController.current
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var dropdownMenuExpended by remember {
        mutableStateOf(false)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.onEvent(IntroduceEvent.UpdateAvatar(it))
        }
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            it.ifTrue { launcher.launch("image/*") }
        }

    LaunchedEffect(Unit) {
        viewModel.onEvent(IntroduceEvent.FetchIntroduce(uid))
    }

    LaunchedEffect(state.logout) {
        if (state.logout) navController.navigateUp()
    }

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
        vm.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.runLauncher) {
        state.runLauncher.handle {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(state.editEvent) {
        state.editEvent.handle {
            navController.navigate(Screen.EditScreen.withArgs(it))
        }
    }


    state.visitAvatar.isNotEmpty().ifTrue {
        AlertDialog(text = {
            AsyncImage(
                model = state.visitAvatar,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LocalSpacing.current.small))
            )
        }, confirmButton = {}, onDismissRequest = {
            viewModel.onEvent(IntroduceEvent.DismissVisitAvatar)
        })
    }

    with(state) {
        verifiedEmailStarting || uploading
    }.ifTrue {
        AlertDialog(confirmButton = {},
            onDismissRequest = {},
            icon = { CircularProgressIndicator() })
    }

    state.verifiedEmailDialogShowing.ifTrue {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Sharp.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            onDismissRequest = {},
            text = {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    enabled = !state.verifiedEmailCodeVerifying,
                    maxLines = 1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(IntroduceEvent.VerifiedEmailCode(code))
                        code = ""
                    }, enabled = code.isNotBlank() and !state.verifiedEmailCodeVerifying
                ) {
                    Text(
                        text = stringResource(
                            id = if (state.verifiedEmailCodeVerifying) R.string.verifying
                            else R.string.confirm
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(IntroduceEvent.CancelVerifiedEmail) },
                    enabled = !state.verifiedEmailCodeVerifying
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            title = {
                val title = state.verifiedEmailCodeMessage.takeIf { it.isNotBlank() }
                    ?: stringResource(id = R.string.email_verified_dialog_title)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            iconContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) { innerPadding ->
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = {
                LazyColumn(Modifier.defaultMinSize(minHeight = 1.dp)) {
                    item {
                        Text(
                            text = state.actionsLabel,
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(LocalSpacing.current.medium)
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
                                .intervalClickable {
                                    scope.launch {
                                        it.onClick()
                                        sheetState.hide()
                                    }
                                })
                    }
                }
            },
            sheetBackgroundColor = MaterialTheme.colorScheme.background,
            sheetContentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                item {
                    Surface(color = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        onClick = {
                            viewModel.onEvent(IntroduceEvent.AvatarClicked)
                            scope.launch {
                                sheetState.show()
                            }
                        }) {
                        val model =
                            ImageRequest.Builder(context).data(state.avatar).crossfade(true).build()
                        SubcomposeAsyncImage(
                            model = model,
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4 / 3f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                item {
                    ProfileList(label = stringResource(id = R.string.account),
                        items = state.dataProperties,
                        onItemClick = { setting ->
                            if (state.isOthers) return@ProfileList
                            if (setting is Property.Data) {
                                viewModel.onEvent(
                                    IntroduceEvent.Actions(
                                        label = setting.key, actions = setting.actions
                                    )
                                )
                                scope.launch {
                                    sheetState.show()
                                }
                            }
                        })
                }

                if (state.settingsProperties.isNotEmpty()) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .height(LocalSpacing.current.medium)
                                .fillMaxWidth()
                                .background(LocalExpandColor.current.divider)
                        )
                    }
                    item {
                        ProfileList(label = stringResource(R.string.settings),
                            items = state.settingsProperties,
                            onItemClick = {})
                    }
                    item {
                        val versionLabel = stringResource(R.string.version_label)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LocalExpandColor.current.divider)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { viewModel.onEvent(IntroduceEvent.ToggleLogMode) },
                                    role = Role.Button
                                ), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$versionLabel${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = LocalSpacing.current.medium),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            ToolBar(
                onNavClick = { navController.navigateUp() },
                actions = {
                    MaterialIconButton(
                        icon = Icons.Sharp.MoreVert,
                        onClick = { dropdownMenuExpended = true },
                        contentDescription = "more"
                    )
                    DropdownMenu(expanded = dropdownMenuExpended,
                        onDismissRequest = { dropdownMenuExpended = false }) {
                        state.isOthers.ifFalse {
                            DropdownMenuItem(onClick = {
                                viewModel.onEvent(IntroduceEvent.SignOut)
                                dropdownMenuExpended = false
                            }, text = {
                                Text(stringResource(R.string.sign_out))
                            })
                        }
                        state.isOthers.ifTrue {
                            DropdownMenuItem(onClick = { dropdownMenuExpended = false }, text = {
                                Text(stringResource(R.string.share))
                            })
                        }
                    }
                },
                text = "",
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }

    BackHandler(sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
        }
    }
}
