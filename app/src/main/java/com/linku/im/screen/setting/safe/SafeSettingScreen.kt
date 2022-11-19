package com.linku.im.screen.setting.safe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.linku.im.R
import com.linku.im.screen.setting.BasicSettingScreen

@Composable
fun SafeSettingScreen(
    modifier: Modifier = Modifier
) {
    BasicSettingScreen(
        title = stringResource(R.string.profile_settings_safe),
        content = {

        },
        modifier = modifier
    )
}
