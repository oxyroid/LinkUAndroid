package com.linku.im.screen.profile.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountItemGroup(
    label: String,
    items: List<Setting>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 8.dp
            ),
            fontSize = 16.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items.forEachIndexed { index, settingItem ->
                AccountItem(settingItem, index != items.size - 1)
            }
        }
    }
}