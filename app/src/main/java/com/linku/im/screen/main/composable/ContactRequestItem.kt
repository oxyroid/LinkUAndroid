package com.linku.im.screen.main.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.linku.domain.bean.ui.ContactRequestUI
import com.linku.im.ui.defaults.ListItemDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRequestItem(
    request: ContactRequestUI,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = request.name
    val message = request.message
    val time = request.time
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15))
    ) {
        ListItem(
            leadingContent = {},
            headlineText = {
                Text(
                    text = message.ifEmpty { "--" },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            overlineText = {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            trailingContent = {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                ),
            colors = ListItemDefault.colors()
        )

    }
}

@Composable
@Preview
fun ContactRequestItemPreview() {
    ContactRequestItem(
        request = ContactRequestUI(
            name = "小蓝",
            message = "约吗？",
            url = "",
            time = "刚刚",
            uid = 1
        ),
        modifier = Modifier.fillMaxWidth(),
        onClick = {}
    )
}
