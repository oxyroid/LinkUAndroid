package com.linku.im.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun Snacker(
    modifier: Modifier = Modifier,
    state: SnackbarHostState,
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
        Tip(
            modifier = modifier.fillMaxWidth(),
            text = rememberedData?.message.orEmpty()
        )
    }
}


