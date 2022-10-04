package com.linku.im.ktx.compose.layout

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round

fun Modifier.boundaries(
    boundaries: Rect
): Modifier = composed {
    val density = LocalDensity.current.density
    Modifier
        .offset { boundaries.topLeft.round() }
        .size(
            width = (boundaries.width / density).dp,
            height = (boundaries.height / density).dp
        )
}