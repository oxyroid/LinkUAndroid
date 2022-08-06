package com.linku.im.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linku.im.ui.theme.seed

@Composable
fun MaterialSnackHost(
    state: SnackbarHostState
) {
    SnackbarHost(
        hostState = state,
        modifier = Modifier.padding(vertical = 12.dp)
    ) { data ->
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = 2.dp,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .height(48.dp),
            backgroundColor = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.message,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )
                data.actionLabel?.also {
                    TextButton(onClick = {
                        data.performAction()
                    }) {
                        Text(
                            text = it,
                            color = seed
                        )
                    }
                }
            }
        }
    }
}