package com.linku.im.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.linku.im.ui.theme.Link

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LinkText(
    text: String,
    url: String,
    onClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        onClick = { onClick.invoke(url) }
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            color = Link,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}