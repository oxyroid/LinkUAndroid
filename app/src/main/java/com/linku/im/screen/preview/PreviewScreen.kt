package com.linku.im.screen.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
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
            .clickable { vm.onEvent(LinkUEvent.PopBackStack) }
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val model = ImageRequest.Builder(LocalContext.current)
            .data(state.url)
            .crossfade(200)
            .build()
        val loader = ImageLoader.Builder(LocalContext.current)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()
        val painter = rememberAsyncImagePainter(
            model = model,
            imageLoader = loader
        )
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f),
            contentScale = ContentScale.FillWidth,
        )
    }
}