package com.linku.im.screen.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.linku.im.R
import com.linku.im.overall
import com.linku.im.screen.overall.OverallEvent
import com.linku.im.screen.login.composable.LoginTextField
import com.linku.im.ui.MaterialButton
import com.linku.im.ui.MaterialTextButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading))

    state.error.handle {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
        loginEvent.handle { overall.onEvent(OverallEvent.PopBackStack) }
    }
}