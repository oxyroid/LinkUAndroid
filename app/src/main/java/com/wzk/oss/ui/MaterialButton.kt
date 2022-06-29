package com.wzk.oss.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MaterialButton(
    textRes: Int,
    modifier: Modifier = Modifier,
    textColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = textRes), color = textColor
        )
    }
}

@Composable
fun MaterialTextButton(
    textRes: Int,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = textRes), color = textColor
        )
    }
}