package com.linku.im.ui.components.notify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay

@Composable
fun NotifyHolder(
    state: SnackbarHostState,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically(initialOffsetY = { it }),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it })
) {
    LaunchedEffect(state.currentSnackbarData) {
        state.currentSnackbarData?.let {
            when (it.duration) {
                SnackbarDuration.Short -> {
                    delay(3_000)
                    state.currentSnackbarData?.dismiss()
                }

                SnackbarDuration.Long -> {
                    delay(5_000)
                    state.currentSnackbarData?.dismiss()
                }

                SnackbarDuration.Indefinite -> {

                }
            }
        } ?: run {
            state.currentSnackbarData?.dismiss()
        }
    }
    AnimatedVisibility(
        visible = state.currentSnackbarData != null,
        enter = enter,
        exit = exit
    ) {
        var rememberedData: SnackbarData? by remember { mutableStateOf(null) }

        LaunchedEffect(state.currentSnackbarData) {
            state.currentSnackbarData?.let {
                rememberedData = it
            }
        }
        NotifyTextHolder(
            modifier = modifier.fillMaxWidth(),
            text = AnnotatedString(rememberedData?.message.orEmpty())
        )
    }
}


