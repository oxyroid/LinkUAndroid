package com.linku.im.screen.main.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linku.im.screen.Screen

internal data class Route(
    @StringRes val titleRes: Int,
    val screen: Screen,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DrawerItem(
    item: Route,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else Color.Unspecified,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(25)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    vertical = 12.dp,
                    horizontal = 18.dp
                )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.icon.name,
                tint = LocalContentColor.current
            )
            Text(
                text = stringResource(item.titleRes),
                modifier = Modifier.padding(start = 12.dp),
                fontWeight = FontWeight.Bold,
                color = LocalContentColor.current
            )
        }
    }

}