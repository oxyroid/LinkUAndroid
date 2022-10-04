package com.linku.im.screen.introduce

import android.Manifest
import android.net.Uri
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Verified
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.Event
import com.linku.im.BuildConfig
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.compose.ui.intervalClickable
import com.linku.im.ktx.ifFalse
import com.linku.im.ktx.ifTrue
import com.linku.im.screen.introduce.composable.ProfileList
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.screen.introduce.util.SquireCropImage
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.components.Snacker
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun IntroduceScreen(
    uid: Int,
    viewModel: IntroduceViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val backStack = LocalBackStack.current
    val context = LocalContext.current

    val uCropLauncher = rememberLauncherForActivityResult(SquireCropImage()) { uri ->
        uri?.let {
            viewModel.onEvent(IntroduceEvent.UpdateAvatar(it))
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uCropLauncher.launch(
                it to Uri.fromFile(
                    File(context.cacheDir, "temp_image_file_${Date().time}")
                )
            )
        }
    }
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        it.ifTrue { launcher.launch("image/*") }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(IntroduceEvent.FetchIntroduce(uid))
    }

    LaunchedEffect(state.logout) {
        if (state.logout)
            backStack.pop()
    }

    LaunchedEffect(state.runLauncher) {
        state.runLauncher.handle { permissionState.launchPermissionRequest() }
    }

    LaunchedEffect(state.editEvent) {
        state.editEvent.handle {
            backStack.push(NavTarget.Edit(it))
        }
    }

    ImagePreviewDialog(state.preview) { viewModel.onEvent(IntroduceEvent.DismissPreview) }

    CircularProgressDialog(enable = with(state) { verifiedEmailStarting || uploading })

    VerifiedEmailDialog(
        enable = state.verifiedEmailDialogShowing,
        verifying = state.verifiedEmailCodeVerifying,
        error = state.verifiedEmailCodeMessage,
        onVerified = { viewModel.onEvent(IntroduceEvent.VerifiedEmailCode(it)) },
        onCanceled = { viewModel.onEvent(IntroduceEvent.CancelVerifiedEmail) }
    )

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    IntroduceScaffold(
        sheetState = sheetState,
        avatar = state.avatar,
        message = viewModel.message,
        isOthers = state.isOthers,
        dataProperties = state.dataProperties,
        settingsProperties = state.settingsProperties,
        topBar = {
            var dropdownMenuExpended by remember { mutableStateOf(false) }
            IntroduceTopBar(
                onDropdownMenuRequest = { dropdownMenuExpended = true },
                onDismissDropdownMenuRequest = { dropdownMenuExpended = false },
                onNavClick = { backStack.pop() },
                ownDropdown = {
                    DropdownMenuItem(
                        onClick = {
                            viewModel.onEvent(IntroduceEvent.SignOut)
                            dropdownMenuExpended = false
                        },
                        text = { Text(stringResource(R.string.sign_out)) }
                    )
                },
                otherDropdown = {
                    DropdownMenuItem(
                        onClick = { dropdownMenuExpended = false },
                        text = { Text(stringResource(R.string.share)) }
                    )
                },
                dropdownMenuExpended = dropdownMenuExpended,
                isOthers = state.isOthers
            )
        },
        sheetContent = {
            IntroduceSheetContent(
                label = state.actionsLabel,
                actions = state.actions,
                onDismissRequest = sheetState::hide
            )
        },
        onPreview = { viewModel.onEvent(IntroduceEvent.AvatarClicked) },
        onAction = { viewModel.onEvent(IntroduceEvent.Actions(it.label, it.actions)) },
        toggleLogMode = { viewModel.onEvent(IntroduceEvent.ToggleLogMode) }
    )

}

@Composable
private fun ImagePreviewDialog(
    model: String,
    onDismissRequest: () -> Unit
) {
    (model.isNotBlank()).ifTrue {
        AlertDialog(
            text = {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LocalSpacing.current.small))
                )
            },
            confirmButton = {},
            onDismissRequest = { onDismissRequest() }
        )
    }
}

@Composable
private fun CircularProgressDialog(enable: Boolean) {
    if (enable)
        AlertDialog(
            confirmButton = {},
            onDismissRequest = {},
            icon = { CircularProgressIndicator() }
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerifiedEmailDialog(
    enable: Boolean,
    verifying: Boolean,
    error: String,
    onVerified: (String) -> Unit,
    onCanceled: () -> Unit
) {
    enable.ifTrue {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Sharp.Verified,
                    contentDescription = null,
                    tint = LocalTheme.current.onSurface
                )
            },
            onDismissRequest = {},
            text = {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = LocalTheme.current.onBackground,
                        containerColor = LocalTheme.current.background
                    ),
                    enabled = !verifying,
                    maxLines = 1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onVerified(code)
                        code = ""
                    },
                    enabled = code.isNotBlank() and !verifying
                ) {
                    Text(
                        text = stringResource(
                            id = if (verifying) R.string.verifying
                            else R.string.confirm
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCanceled,
                    enabled = !verifying
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            title = {
                val title = error.takeIf { it.isNotBlank() }
                    ?: stringResource(id = R.string.email_verified_dialog_title)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            },
            containerColor = LocalTheme.current.surface,
            titleContentColor = LocalTheme.current.onSurface,
            iconContentColor = LocalTheme.current.onSurface,
            textContentColor = LocalTheme.current.onSurface
        )
    }
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun IntroduceScaffold(
    isOthers: Boolean,
    sheetState: ModalBottomSheetState,
    avatar: String,
    dataProperties: List<Property>,
    settingsProperties: List<Property>,
    message: Event<String>,
    topBar: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    onPreview: () -> Unit,
    onAction: (IntroduceEvent.Actions) -> Unit,
    toggleLogMode: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(message, vm.message) {
        message.handle { scaffoldState.snackbarHostState.showSnackbar(it) }
        vm.message.handle { scaffoldState.snackbarHostState.showSnackbar(it) }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(sheetState.isVisible) { scope.launch { sheetState.hide() } }
    Scaffold(
        snackbarHost = {
            Snacker(
                state = it,
                modifier = Modifier.fillMaxWidth()
            )
        },
        scaffoldState = scaffoldState,
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = sheetContent,
            sheetBackgroundColor = LocalTheme.current.background,
            sheetContentColor = LocalTheme.current.onBackground,
            modifier = Modifier
                .padding(innerPadding)
                .background(LocalTheme.current.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    Surface(
                        color = LocalTheme.current.primary,
                        contentColor = LocalTheme.current.onPrimary,
                        onClick = {
                            onPreview()
                            scope.launch { sheetState.show() }
                        }
                    ) {
                        val model = ImageRequest.Builder(context)
                            .data(avatar)
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
                        label = stringResource(R.string.account),
                        items = dataProperties,
                        onItemClick = { setting ->
                            if (isOthers) return@ProfileList
                            if (setting is Property.Data) {
                                IntroduceEvent.Actions(
                                    label = setting.key,
                                    actions = setting.actions
                                ).also(onAction)
                                scope.launch { sheetState.show() }
                            }
                        }
                    )
                }

                if (settingsProperties.isNotEmpty()) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .height(LocalSpacing.current.medium)
                                .fillMaxWidth()
                                .background(LocalTheme.current.divider)
                        )
                    }
                    item {
                        val backStack = LocalBackStack.current
                        ProfileList(
                            label = stringResource(R.string.settings),
                            items = settingsProperties,
                            onItemClick = { property ->
                                when (property) {
                                    is Property.Data -> {}
                                    is Property.Folder -> backStack.push(property.setting)
                                }
                            }
                        )
                    }
                    item {
                        val versionLabel = stringResource(R.string.version_label)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LocalTheme.current.divider)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = toggleLogMode,
                                    role = Role.Button
                                ), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$versionLabel${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = LocalSpacing.current.medium),
                                color = LocalTheme.current.onBackground
                            )
                        }
                    }
                }
            }
            topBar()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun IntroduceSheetContent(
    label: String,
    actions: List<Property.Data.Action>,
    onDismissRequest: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn(Modifier.defaultMinSize(minHeight = 1.dp)) {
        item {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(color = LocalTheme.current.primary),
                modifier = Modifier.padding(LocalSpacing.current.medium)
            )
        }
        items(actions) {
            ListItem(
                text = {
                    Text(
                        text = it.text,
                        color = LocalTheme.current.onBackground
                    )
                },
                icon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = it.text,
                        tint = LocalTheme.current.onBackground
                    )
                },
                modifier = Modifier
                    .background(LocalTheme.current.background)
                    .intervalClickable {
                        scope.launch {
                            it.onClick()
                            onDismissRequest()
                        }
                    }
            )
        }
    }
}

@Composable
private fun IntroduceTopBar(
    isOthers: Boolean,
    dropdownMenuExpended: Boolean,
    onDropdownMenuRequest: () -> Unit,
    onDismissDropdownMenuRequest: () -> Unit,
    onNavClick: () -> Unit,
    otherDropdown: @Composable ColumnScope.() -> Unit,
    ownDropdown: @Composable ColumnScope.() -> Unit
) {
    ToolBar(
        onNavClick = onNavClick,
        actions = {
            MaterialIconButton(
                icon = Icons.Sharp.MoreVert,
                onClick = onDropdownMenuRequest,
                contentDescription = null
            )
            DropdownMenu(
                expanded = dropdownMenuExpended,
                onDismissRequest = onDismissDropdownMenuRequest
            ) {
                isOthers.ifFalse { ownDropdown() }
                isOthers.ifTrue { otherDropdown() }
            }
        },
        text = "",
        backgroundColor = Color.Transparent,
        contentColor = LocalTheme.current.onSurface
    )
}
