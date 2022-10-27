package com.linku.im.screen.sign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.ktx.compose.runtime.ComposableLifecycle
import com.linku.im.ui.components.PasswordTextField
import com.linku.im.ui.components.TextField
import com.linku.im.ui.components.button.MaterialButton
import com.linku.im.ui.components.button.MaterialPremiumButton
import com.linku.im.ui.components.button.MaterialTextButton
import com.linku.im.ui.components.notify.NotifyHolder
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SignScreen(
    viewModel: SignViewModel = hiltViewModel()
) {
    val systemUiController = rememberSystemUiController()
    val theme = LocalTheme.current
    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_CREATE) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !theme.isDark
            )
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = theme.isDarkText
            )
        }
    }

    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()
    val backStack = LocalBackStack.current
    val lottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            viewModel.restore()
        }
    }

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
        vm.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.loginEvent) {
        state.loginEvent.handle {
            backStack.pop()
        }
    }

    val xray by viewModel.point3DFlow.collectAsStateWithLifecycle(0f)

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            NotifyHolder(
                state = it,
                modifier = Modifier.fillMaxWidth()
            )
        },
        backgroundColor = LocalTheme.current.background,
        contentColor = LocalTheme.current.onBackground
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(PaddingValues(horizontal = LocalSpacing.current.extraLarge)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var isDragging by remember {
                    mutableStateOf(false)
                }
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current.density
                val fullWidth = configuration.screenWidthDp * density
                var offset by remember {
                    mutableStateOf(fullWidth / 2f)
                }
                val animateProgress by animateFloatAsState(
                    (offset / fullWidth + xray * 0.15f).coerceIn(0f..1f),
                    spring(
                        dampingRatio = when (isDragging) {
                            true -> Spring.DampingRatioNoBouncy
                            false -> Spring.DampingRatioHighBouncy
                        },
                        stiffness = Spring.StiffnessVeryLow
                    )
                )
                val zoom by animateFloatAsState(
                    if (isDragging) 0.8f else 1f
                )
                var premiumVisible by remember { mutableStateOf(false) }
                val draggableState = rememberDraggableState {
                    offset -= it
                }
                LaunchedEffect(animateProgress) {
                    if (animateProgress >= 0.75f || animateProgress <= 0.25f) {
                        premiumVisible = true
                    }
                }
                val feedback = LocalHapticFeedback.current
                LottieAnimation(
                    composition = lottie,
                    progress = { animateProgress },
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                        }
                        .padding(bottom = LocalSpacing.current.medium)
                        .fillMaxWidth()
                        .height(160.dp)
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Horizontal,
                            onDragStopped = {
                                offset = fullWidth / 2f
                                feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isDragging = false
                            },
                            onDragStarted = {
                                feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDragging = true
                            }
                        )
                )
                TextField(
                    background = LocalTheme.current.surface,
                    textFieldValue = state.email,
                    onValueChange = { viewModel.onEvent(SignEvent.OnEmail(it)) },
                    placeholder = stringResource(id = R.string.screen_login_tag_email),
                    enabled = !state.loading,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusRequester.requestFocus()
                        }
                    )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalSpacing.current.medium)
                )

                PasswordTextField(
                    textFieldValue = state.password,
                    onValueChange = { viewModel.onEvent(SignEvent.OnPassword(it)) },
                    placeholder = stringResource(id = R.string.screen_login_tag_password),
                    enabled = !state.loading,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onEvent(SignEvent.SignIn)
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.focusRequester(focusRequester)
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalSpacing.current.extraLarge)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)
                ) {
                    MaterialButton(
                        text = run {
                            val syncing = state.syncing
                            if (syncing) {
                                stringResource(R.string.syncing)
                            } else {
                                stringResource(R.string.screen_login_btn_login)
                            }
                        },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.onEvent(SignEvent.SignIn)
                        focusManager.clearFocus()
                    }
                    AnimatedVisibility(premiumVisible) {
                        MaterialPremiumButton(
                            text = "注册并订阅Premium (15% off)",
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            vm.onEvent(LinkUEvent.Premium)
                            focusManager.clearFocus()
                        }
                    }
                    MaterialTextButton(
                        textRes = R.string.screen_login_btn_register,
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.onEvent(SignEvent.SignUp)
                        focusManager.clearFocus()
                    }
                }


                Spacer(modifier = Modifier.height(LocalSpacing.current.large))
            }
        }
    }

}
