package com.linku.im.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import com.linku.im.screen.global.GlobalViewModel
import com.linku.im.R
import com.linku.im.screen.login.composable.LoginTextField
import com.linku.im.ui.MaterialButton
import com.linku.im.ui.MaterialTextButton

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel
) {
    val state by viewModel.state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading))

    with(globalViewModel) {
        icon.value = Icons.Default.ArrowBack
        title.value = state.title
        navClick.value = {
            navController.popBackStack()
        }
        actions.value = {
            IconButton(
                onClick = {

                }
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
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
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            )
            LoginTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                titleRes = R.string.screen_login_tag_password,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
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
                viewModel.onEvent(LoginEvent.Login(email, password))
            }
            MaterialTextButton(
                textRes = R.string.screen_login_btn_register,
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                viewModel.onEvent(LoginEvent.Register(email, password))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    with(state) {
        loginEvent.handle { navController.popBackStack() }
    }
}