package com.linku.im.screen.sign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.ui.Scaffold
import com.linku.im.R
import com.linku.im.ui.components.MaterialButton
import com.linku.im.ui.components.MaterialTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: SignViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val scaffoldState = rememberScaffoldState()
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading))

    val focusRequester = remember(::FocusRequester)

    LaunchedEffect(state.error) {
        state.error.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState
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
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(160.dp),
                    iterations = LottieConstants.IterateForever
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(SignEvent.OnEmail(it)) },
                    label = { Text(stringResource(R.string.screen_login_tag_email)) },
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold
                    ),
                    enabled = !state.loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            // TODO
                        }
                    )
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(SignEvent.OnPassword(it)) },
                    label = { Text(stringResource(R.string.screen_login_tag_password)) },
                    modifier = Modifier
                        .padding(horizontal = 48.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold
                    ),
                    enabled = !state.loading,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onEvent(SignEvent.SignIn)
                            focusRequester.captureFocus()
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                MaterialButton(
                    textRes = R.string.screen_login_btn_login,
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    viewModel.onEvent(SignEvent.SignIn)
                    focusRequester.captureFocus()
                }
                MaterialTextButton(
                    textRes = R.string.screen_login_btn_register,
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    viewModel.onEvent(SignEvent.SignUp)
                    focusRequester.captureFocus()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}