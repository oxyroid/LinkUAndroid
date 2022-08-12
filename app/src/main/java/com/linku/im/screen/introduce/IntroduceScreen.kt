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
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.extension.intervalClickable
import com.linku.im.screen.introduce.composable.ProfileList
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.ToolBarAction
import com.linku.im.ui.theme.divider
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun ProfileScreen(
    viewModel: IntroduceViewModel = hiltViewModel()
) {
    val vmState = vm.readable
    val context = LocalContext.current
    val isDarkMode = vmState.isDarkMode
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

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
        viewModel.onEvent(IntroduceEvent.FetchIntroduce)
    }

    LaunchedEffect(state.logout) {
        if (state.logout) vm.onEvent(LinkUEvent.PopBackStack)
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

    state.visitAvatar.isNotEmpty().ifTrue {
        AlertDialog(
            text = {
                AsyncImage(
                    model = state.visitAvatar,
                    contentDescription = "",
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
            },
            confirmButton = {},
            onDismissRequest = {
                viewModel.onEvent(IntroduceEvent.DismissVisitAvatar)
            }
        )
    }

    with(state) {
        verifiedEmailStarting || uploading
    }.ifTrue {
        AlertDialog(
            text = { CircularProgressIndicator() },
            confirmButton = {},
            onDismissRequest = {},
        )
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
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(state.actions) {
                        ListItem(
                            text = {
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
                                }
                        )
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
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        onClick = {
                            viewModel.onEvent(IntroduceEvent.AvatarClicked)
                            scope.launch {
                                sheetState.show()
                            }
                        }
                    ) {
                        val model = ImageRequest.Builder(context)
                            .data(state.avatar)
                            .crossfade(true)
                            .build()
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
                    ProfileList(
                        label = stringResource(id = R.string.account),
                        items = state.dataProperties,
                        onItemClick = { setting ->
                            if (setting is Property.Data) {
                                viewModel.onEvent(
                                    IntroduceEvent.Actions(
                                        label = setting.key,
                                        actions = setting.actions
                                    )
                                )
                                scope.launch {
                                    sheetState.show()
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .height(18.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.divider(isDarkMode))
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
                            .background(MaterialTheme.colorScheme.divider(isDarkMode))
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { viewModel.onEvent(IntroduceEvent.ToggleLogMode) },
                                role = Role.Button
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$versionLabel${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            ToolBar(
                onNavClick = { vm.onEvent(LinkUEvent.PopBackStack) },
                actions = {
                    ToolBarAction(
                        onClick = { dropdownMenuExpended = true },
                        imageVector = Icons.Sharp.MoreVert,
                    )
                    DropdownMenu(
                        expanded = dropdownMenuExpended,
                        onDismissRequest = { dropdownMenuExpended = false }
                    ) {
                        DropdownMenuItem(
                            onClick = { viewModel.onEvent(IntroduceEvent.SignOut) },
                            text = {
                                Text(stringResource(R.string.sign_out))
                            }
                        )
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
