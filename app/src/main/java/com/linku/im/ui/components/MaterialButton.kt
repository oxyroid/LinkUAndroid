package com.linku.im.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.im.ui.theme.LocalTheme

@Composable
fun MaterialButton(
    textRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = LocalTheme.current.primary,
            contentColor = LocalTheme.current.onPrimary,
            disabledContainerColor = LocalTheme.current.primaryDisable,
            disabledContentColor = LocalTheme.current.onPrimaryDisable
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
    onClick: () -> Unit
) {
    TextButton(
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = LocalTheme.current.primary,
            disabledContentColor = LocalTheme.current.primaryDisable
        )
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(id = textRes),
            fontSize = 14.sp
        )
    }
}