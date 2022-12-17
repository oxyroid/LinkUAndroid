package com.linku.im.screen.introduce

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Verified
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import com.linku.core.extension.ifTrue
import com.linku.core.wrapper.Event
import com.linku.im.BuildConfig
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.screen.introduce.composable.ProfileList
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.screen.introduce.util.SquireCropImage
import com.linku.im.ui.components.BottomSheetContent
import com.linku.im.ui.components.Scrim
import com.linku.im.ui.components.MaterialTopBar
import com.linku.im.ui.components.button.MaterialButton
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.components.notify.NotifyCompat
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Composable
fun IntroduceScreen(
    uid: Int,
    modifier: Modifier = Modifier,
    viewModel: IntroduceViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val backStack = LocalBackStack.current
    val context = LocalContext.current

    val uCropLauncher = rememberLauncherForActivityResult(SquireCropImage()) { uri ->
        uri?.let { viewModel.onEvent(IntroduceEvent.UpdateAvatar(it)) }
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val file = File(context.cacheDir, "temp_image_file_${Date().time}")
                uCropLauncher.launch(
                    it to Uri.fromFile(file)
                )
            }
        }

    LaunchedEffect(Unit) {
        viewModel.onEvent(IntroduceEvent.FetchIntroduce(uid))
    }

    LaunchedEffect(Unit) {
        viewModel.signOutFlow.collectLatest {
            if (it) {
                backStack.pop()
            }
        }
    }

    LaunchedEffect(state.runLauncher) {
        state.runLauncher.handle { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    }

    LaunchedEffect(state.goChat) {
        state.goChat.handle {
            backStack.singleTop(NavTarget.ChatTarget.Messages(it))
        }
    }

    LaunchedEffect(state.editEvent) {
        state.editEvent.handle {
            backStack.push(NavTarget.Edit(it))
        }
    }

    CircularProgressDialog(enable = with(state) { verifiedEmailStarting || uploading })

    VerifiedEmailDialog(
        enable = state.verifiedEmailDialogShowing,
        verifying = state.verifiedEmailCodeVerifying,
        error = state.verifiedEmailCodeMessage,
        onVerified = { viewModel.onEvent(IntroduceEvent.VerifiedEmailCode(it)) },
        onCanceled = { viewModel.onEvent(IntroduceEvent.CancelVerifiedEmail) }
    )

    val (expanded, onExpanded) = remember {
        mutableStateOf(false)
    }

    IntroduceScaffold(
        expanded = expanded,
        onExpanded = onExpanded,
        label = state.actionsLabel,
        actions = state.actions,
        avatar = state.avatar,
        message = viewModel.message,
        category = state.category,
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
                category = state.category
            )
        },
        onPreview = { viewModel.onEvent(IntroduceEvent.AvatarClicked) },
        onAction = { viewModel.onEvent(IntroduceEvent.Actions(it.label, it.actions)) },
        toggleLogMode = { viewModel.onEvent(IntroduceEvent.ToggleLogMode) },
        onFriendshipAction = { viewModel.onEvent(IntroduceEvent.FriendShipAction) },
        modifier = modifier
    )
    BackHandler(expanded) { onExpanded(false) }
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
    val theme = LocalTheme.current
    enable.ifTrue {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = theme.onSurface
                )
            },
            onDismissRequest = {},
            text = {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.onBackground,
                        containerColor = theme.background
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
            containerColor = theme.surface,
            titleContentColor = theme.onSurface,
            iconContentColor = theme.onSurface,
            textContentColor = theme.onSurface
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
    label: String,
    actions: List<Property.Data.Action>,
    expanded: Boolean,
    onExpanded: (Boolean) -> Unit,
    category: Category,
    avatar: String,
    dataProperties: List<Property>,
    settingsProperties: List<Property>,
    message: Event<String>,
    topBar: @Composable () -> Unit,
    onPreview: () -> Unit,
    onAction: (IntroduceEvent.Actions) -> Unit,
    toggleLogMode: () -> Unit,
    onFriendshipAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaffoldState = rememberScaffoldState()
    val spacing = LocalSpacing.current
    LaunchedEffect(message, vm.message) {
        message.handle { scaffoldState.snackbarHostState.showSnackbar(it) }
        vm.message.handle { scaffoldState.snackbarHostState.showSnackbar(it) }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme = LocalTheme.current

    Scaffold(
        snackbarHost = { NotifyCompat(state = it) },
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        backgroundColor = theme.background,
        contentColor = theme.onBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                Surface(
                    color = theme.primary,
                    contentColor = theme.onPrimary,
                    onClick = {
                        onPreview()
                        scope.launch { onExpanded(true) }
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
                        if (category is Category.User) return@ProfileList
                        if (setting is Property.Data) {
                            IntroduceEvent.Actions(
                                label = setting.key,
                                actions = setting.actions
                            ).also(onAction)
                            scope.launch { onExpanded(true) }
                        }
                    }
                )
            }

            if (settingsProperties.isNotEmpty()) {
                item {
                    Spacer(
                        modifier = Modifier
                            .height(spacing.medium)
                            .fillMaxWidth()
                            .background(theme.divider)
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
                            .background(theme.divider)
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
                            modifier = Modifier.padding(vertical = spacing.medium),
                            color = theme.onBackground
                        )
                    }
                }
            }
        }

        when (category) {
            Category.Personal -> {}
            is Category.User -> {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(spacing.medium)
                ) {
                    val state = category.friendship
                    MaterialButton(
                        enabled = state != Friendship.Loading &&
                                (state !is Friendship.Pending || state.isReceived),
                        textRes = when (state) {
                            Friendship.Loading -> R.string.friendship_loading
                            Friendship.None -> R.string.friendship_none
                            is Friendship.Pending -> if (state.isReceived) R.string.friendship_pending_received
                            else R.string.friendship_pending_sent

                            is Friendship.Completed -> R.string.friendship_completed
                        },
                        onClick = onFriendshipAction,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        topBar()

        Scrim(
            color = Color.Black * 0.35f,
            onDismiss = { onExpanded(false) },
            visible = expanded
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            BottomSheetContent(
                visible = expanded,
                onDismiss = { onExpanded(false) },
                maxHeight = false,
                modifier = Modifier,
                content = {
                    LazyColumn(Modifier.defaultMinSize(minHeight = 1.dp)) {
                        item {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = theme.primary
                                ),
                                modifier = Modifier.padding(spacing.medium)
                            )
                        }
                        items(actions) {
                            ListItem(
                                text = {
                                    Text(
                                        text = it.text,
                                        color = theme.onBackground
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = it.icon,
                                        contentDescription = it.text,
                                        tint = theme.onBackground * 0.65f
                                    )
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.medium))
                                    .background(theme.background)
                                    .combinedClickable(
                                        onClick = {
                                            it.onClick()
                                            onExpanded(false)
                                        }
                                    )
                            )
                        }
                    }
                })
        }
    }
}

@Composable
private fun IntroduceTopBar(
    category: Category,
    dropdownMenuExpended: Boolean,
    onDropdownMenuRequest: () -> Unit,
    onDismissDropdownMenuRequest: () -> Unit,
    onNavClick: () -> Unit,
    otherDropdown: @Composable ColumnScope.() -> Unit,
    ownDropdown: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalTheme.current
    MaterialTopBar(
        onNavClick = onNavClick,
        actions = {
            MaterialIconButton(
                icon = Icons.Rounded.MoreVert,
                onClick = onDropdownMenuRequest,
                contentDescription = null
            )
            DropdownMenu(
                expanded = dropdownMenuExpended,
                onDismissRequest = onDismissDropdownMenuRequest
            ) {
                when (category) {
                    Category.Personal -> ownDropdown()
                    is Category.User -> otherDropdown()
                }
            }
        },
        text = "",
        backgroundColor = Color.Transparent,
        contentColor = theme.onSurface
    )
}
