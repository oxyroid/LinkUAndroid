package com.linku.im.screen.setting.data

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.linku.im.R
import com.linku.im.screen.setting.BasicSettingScreen
import com.linku.im.screen.setting.common.CheckBoxItem

@Composable
fun DataStorageSettingScreen(
    modifier: Modifier = Modifier
) {
    BasicSettingScreen(
        title = stringResource(R.string.profile_settings_datasource),
        modifier = modifier,
        content = {
            var checked by remember {
                mutableStateOf(false)
            }
            CheckBoxItem(
                title = "始终加载原图",
                enabled = true,
                checked = checked,
                onCheckedChange = {
                    checked = it
                }
            )
        }
    )
}
