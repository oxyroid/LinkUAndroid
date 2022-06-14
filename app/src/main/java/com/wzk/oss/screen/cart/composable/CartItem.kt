package com.wzk.oss.screen.cart.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.wzk.domain.entity.Food

@Composable
fun CartItem(
    food: Food,
    modifier: Modifier = Modifier
) {
    Column {
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        Surface(modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 24.dp,
                        horizontal = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = food.img,
                    contentDescription = food.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(64.dp)
                        .aspectRatio(1f)
                )

                Text(
                    text = food.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }

}