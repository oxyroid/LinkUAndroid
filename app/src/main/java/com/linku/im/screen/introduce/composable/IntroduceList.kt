package com.linku.im.screen.introduce.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linku.im.ui.theme.LocalExpandColor
import com.linku.im.ui.theme.LocalTheme

@Composable
fun ProfileList(
    label: String,
    items: List<Property>,
    onItemClick: (Property) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalTheme.current.background)
            .padding(top = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = LocalTheme.current.primary,
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 4.dp
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, settingItem ->
                IntroduceItem(
                    property = settingItem,
                    onClick = {
                        onItemClick(settingItem)
                    }
                )
                if (index != items.lastIndex)
                    Divider(
                        thickness = 0.6.dp,
                        color = LocalExpandColor.current.divider
                    )
            }
        }
    }
}