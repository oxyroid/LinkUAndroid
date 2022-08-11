package com.linku.im.screen.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.linku.im.LinkUEvent
import com.linku.im.vm

@Composable
fun PreviewScreen(
    mid: Int,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    LaunchedEffect(Unit) {
        viewModel.onEvent(PreviewEvent.GetImageUrl(mid))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                vm.onEvent(LinkUEvent.PopBackStack)
            }
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = state.url,
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}