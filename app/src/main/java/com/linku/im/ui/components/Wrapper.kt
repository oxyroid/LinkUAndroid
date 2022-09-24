package com.linku.im.ui.components

import androidx.compose.runtime.Composable

@Composable
fun Wrapper(content: @Composable () -> Unit) {
    content()
}