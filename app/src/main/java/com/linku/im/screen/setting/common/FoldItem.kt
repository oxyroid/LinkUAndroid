package com.linku.im.screen.setting.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linku.im.ktx.ui.intervalClickable
import com.linku.im.ui.defaults.ListItemDefault
import com.linku.im.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldItem(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {}
) {
    val spacing = LocalSpacing.current
    ListItem(
        headlineText = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingContent = trailingContent,
        colors = ListItemDefault.colors(enabled = enabled),
        modifier = modifier
            .fillMaxWidth()
            .intervalClickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(start = spacing.medium)
    )
}

@Composable
fun CheckBoxItem(
    title: String,
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FoldItem(
        title = title,
        enabled = enabled,
        onClick = {
            if (enabled) {
                onCheckedChange(!checked)
            }
        },
        modifier = modifier,
        trailingContent = {
            Checkbox(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
