package com.wzk.oss.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wzk.oss.R
import com.wzk.oss.application
import com.wzk.oss.screen.login.composable.LoginScreenBar
import com.wzk.oss.screen.login.composable.LoginTextField
import com.wzk.oss.ui.MaterialButton
import com.wzk.oss.ui.MaterialSnackHost
import com.wzk.oss.ui.MaterialTextButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading))
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            LoginScreenBar {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            MaterialSnackHost(state = scaffoldState.snackbarHostState)
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(160.dp),
                    iterations = LottieConstants.IterateForever
                )
                LoginTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    titleRes = R.string.screen_login_tag_email,
                    modifier = Modifier.fillMaxWidth()
                )
                LoginTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    titleRes = R.string.screen_login_tag_password,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                MaterialButton(
                    textRes = R.string.screen_login_btn_login,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    viewModel.onEvent(LoginEvent.Login(email, password))
                }
                MaterialTextButton(
                    textRes = R.string.screen_login_btn_register,
                    textColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    viewModel.onEvent(LoginEvent.Register(email, password))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    with(state) {
        loginEvent.handle { navController.popBackStack() }
        registerEvent.handle {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    application.getString(R.string.register_success)
                )
            }
        }
        error.handle {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
        }
    }
}