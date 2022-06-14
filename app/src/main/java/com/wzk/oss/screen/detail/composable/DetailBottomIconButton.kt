package com.wzk.oss.screen.detail.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DetailBottomIconButton(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(onClick = onClick),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .size(56.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                modifier = Modifier
                    .aspectRatio(1f)
                    .weight(1f)
            )
            Text(
                text = text,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                style = TextStyle(
                    textAlign = TextAlign.Center
                )
            )
        }
    }

}