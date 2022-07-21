package com.linku.im.screen.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.linku.im.R
import com.linku.im.application
import com.linku.im.extension.debug
import com.linku.im.extension.release
import com.linku.im.ui.LinkText
import com.linku.im.ui.MaterialButton
import com.linku.im.vm
import com.mukesh.MarkDown
import java.util.*

@Composable
fun InfoScreen() {
    vm.onActions { }
    val markdown: String = remember {
        application.assets.open("info.md").use {
            val scanner = Scanner(it).useDelimiter("\\A")
            if (scanner.hasNext()) scanner.next() else ""
        }
    }
    var visible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.developer))
        LottieAnimation(
            composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .padding(top = 24.dp)
                .weight(2f)
                .aspectRatio(3 / 4f)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .weight(3f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                MarkDown(text = markdown)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        release {
            val text by derivedStateOf {
                if (visible) R.string.contract_me_by_follow else R.string.for_source
            }
            MaterialButton(
                textRes = text,
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
//                        outsideContent.startActivity(Intent().apply {
//                            action = Intent.ACTION_VIEW
//                            data = Uri.parse(it)
//                        })
                    }
                    LinkText(text = "Telegram:sortBy", url = "https://t.me/sortBy") {
//                        outsideContent.startActivity(Intent().apply {
//                            action = Intent.ACTION_VIEW
//                            data = Uri.parse(it)
//                        })
                    }
                }
            }
        }
        debug {
            MaterialButton(
                textRes = R.string.code_source,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
//                outsideContent.startActivity(Intent().apply {
//                    action = Intent.ACTION_VIEW
//                    data = Uri.parse("https://github.com/thxbrop/LinkU-Android")
//                })
            }
        }


    }
}