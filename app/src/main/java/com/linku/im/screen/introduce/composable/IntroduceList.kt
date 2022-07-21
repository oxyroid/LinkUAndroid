package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileList(
    label: String,
    items: List<Property>,
    onItemClick: (Property) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 4.dp
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items.forEachIndexed { index, settingItem ->
                IntroduceItem(
                    property = settingItem,
                    onClick = {
                        onItemClick(settingItem)
                    },
                    divider = index != items.size - 1
                )
            }
        }
    }
}