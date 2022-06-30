package com.linku.im.screen.info

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.linku.im.activity
import com.linku.im.application
import com.linku.im.ui.LinkText
import com.linku.im.ui.MaterialButton
import com.linku.im.ui.MaterialTopBar
import com.mukesh.MarkDown
import com.linku.im.R
import java.util.*

@Composable
fun InfoScreen(
    navController: NavController
) {
    val markdown: String = remember {
        application.assets.open("info.md").use {
            val scanner = Scanner(it).useDelimiter("\\A")
            if (scanner.hasNext()) {
                scanner.next()
            } else ""
        }
    }
    var visible by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            MaterialTopBar(title = stringResource(id = R.string.info)) {
                navController.popBackStack()
            }
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.developer))
            LottieAnimation(
                composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .weight(2f)
                    .aspectRatio(3 / 4f)
            )
            MarkDown(
                text = markdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .weight(3f)
            )
            MaterialButton(
                textRes = if (visible) R.string.contract_me_by_follow else R.string.for_source,
                textColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                visible = !visible
            }

            AnimatedVisibility(
                visible = visible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row {
                    LinkText(text = "Github:thxbrop", url = "https://github.com/thxbrop") {
                        activity.startActivity(Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(it)
                        })
                    }
                    LinkText(text = "Telegram:sortBy", url = "https://t.me/sortBy") {
                        activity.startActivity(Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(it)
                        })
                    }
                }
            }

        }
    }
}