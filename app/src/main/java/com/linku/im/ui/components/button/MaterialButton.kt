package com.linku.im.ui.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.im.ui.brush.premiumBrush
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
    MaterialButton(
        text = stringResource(textRes),
        modifier = modifier,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        onClick = onClick
    )
}

@Composable
fun MaterialButton(
    text: String,
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
            text = text
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
    disabledContentColor: Color = LocalTheme.current.onSurface.copy(alpha = 0.38f),
    onClick: () -> Unit
) {
    MaterialTextButton(
        text = stringResource(textRes),
        modifier = modifier,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        onClick = onClick
    )
}

@Composable
fun MaterialTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalTheme.current.primary,
    disabledContainerColor: Color = Color.Transparent,
    disabledContentColor: Color = LocalTheme.current.onSurface.copy(alpha = 0.38f),
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
            text = text,
            fontSize = 14.sp
        )
    }
}


@Composable
fun MaterialPremiumButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val brush = premiumBrush()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinHeight
                        )
                        .padding(ButtonDefaults.ContentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Text(
                            text = text
                        )
                    }
                )
            }
        }
    }
}
