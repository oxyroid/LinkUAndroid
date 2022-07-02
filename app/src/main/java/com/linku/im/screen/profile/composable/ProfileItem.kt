package com.linku.im.screen.profile.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linku.im.extension.ifTrue
import com.linku.im.extension.times
import com.linku.im.ui.theme.SubTitleFontSize
import com.linku.im.ui.theme.TitleFontSize

private val DIVIDER = 0.6.dp
private val PADDING_X = 24.dp
private val ENTITY_PADDING_Y = 8.dp
private val FOLDER_PADDING_Y = 16.dp
private const val TINT_ALPHA = 0.8f

@Composable
fun ProfileItem(
    setting: Setting,
    divider: Boolean = true
) {
    when (setting) {
        is Setting.Entity -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = setting.onClick)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        start = PADDING_X, top = ENTITY_PADDING_Y
                    )
            ) {
                Text(
                    text = setting.key,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = TitleFontSize,
                    modifier = Modifier.padding(end = PADDING_X)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = setting.value,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = SubTitleFontSize,
                    modifier = Modifier.padding(
                        end = PADDING_X,
                        bottom = if (divider) 0.dp else ENTITY_PADDING_Y
                    )
                )
                divider.ifTrue {
                    Divider(
                        thickness = DIVIDER,
                        color = MaterialTheme.colorScheme.outline * 0.6f,
                        modifier = Modifier.padding(top = ENTITY_PADDING_Y, end = 8.dp)
                    )
                }
            }
        }
        is Setting.Folder -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(onClick = {

                    })
                    .padding(
                        start = PADDING_X,
                        top = FOLDER_PADDING_Y
                    )
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = setting.icon.name,
                    tint = MaterialTheme.colorScheme.outline * TINT_ALPHA
                )
                Column(
                    modifier = Modifier.padding(start = PADDING_X)
                ) {
                    Text(
                        text = setting.label,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = TitleFontSize,
                        modifier = Modifier.padding(
                            end = PADDING_X,
                            bottom = if (divider) 0.dp else FOLDER_PADDING_Y
                        )
                    )
                    divider.ifTrue {
                        Divider(
                            thickness = DIVIDER,
                            color = MaterialTheme.colorScheme.outline * 0.6f,
                            modifier = Modifier.padding(top = FOLDER_PADDING_Y, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}