package com.wzk.oss.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost as SnackbarHostM3
import androidx.compose.material3.SnackbarHostState as SnackbarHostStateM3

@Composable
fun MaterialSnackHost(state: SnackbarHostState) {
    SnackbarHost(
        hostState = state,
        modifier = Modifier.padding(vertical = 12.dp)
    ) { data ->
        Card(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(64.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.message,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                data.actionLabel?.also {
                    TextButton(onClick = {
                        data.performAction()
                    }) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MySnackHostM3(state: SnackbarHostStateM3) {
    SnackbarHostM3(
        hostState = state,
        modifier = Modifier.padding(vertical = 12.dp)
    ) { data ->
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(64.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                data.visuals.actionLabel?.also {
                    TextButton(onClick = {
                        data.performAction()
                    }) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}