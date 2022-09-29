package com.linku.im.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.im.ui.theme.LocalTheme

@Composable
fun MaterialButton(
    textRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = LocalTheme.current.primary,
    contentColor: Color = LocalTheme.current.onPrimary,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.12f),
    disabledContentColor: Color = containerColor.copy(alpha = 0.38f),
    onClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        Text(
            text = stringResource(id = textRes)
        )
    }
}

@Composable
fun MaterialTextButton(
    textRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalTheme.current.primary,
    disabledContainerColor: Color = Color.Transparent,
    disabledContentColor: Color = containerColor.copy(alpha = 0.38f),
    onClick: () -> Unit
) {
    TextButton(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(id = textRes),
            fontSize = 14.sp
        )
    }
}