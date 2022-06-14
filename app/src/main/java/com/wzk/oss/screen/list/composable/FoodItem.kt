package com.wzk.oss.screen.list.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.wzk.oss.ui.shimmerBrush
import com.wzk.domain.entity.Food

@Composable
fun FoodShimmerItem(
    food: Food,
    onSelect: (Int) -> Unit
) {
    Surface(
        elevation = 0.dp,
        shape = RectangleShape,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(vertical = 1.dp, horizontal = 0.5.dp)
            .clickable { onSelect.invoke(food.id) },
        color = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    ) {
        SubcomposeAsyncImage(
            model = food.img,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(shimmerBrush())
                )
            }
        )
    }
}