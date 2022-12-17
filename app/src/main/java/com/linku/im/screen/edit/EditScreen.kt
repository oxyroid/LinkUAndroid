package com.linku.im.screen.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.linku.im.R
import com.linku.im.screen.introduce.IntroduceEvent
import com.linku.im.screen.setting.BasicSettingScreen

@Composable
fun EditScreen(
    type: IntroduceEvent.Edit.Type,
    modifier: Modifier = Modifier
) {
    BasicSettingScreen(
        title = when (type) {
            IntroduceEvent.Edit.Type.Description -> stringResource(R.string.profile_data_description)
            IntroduceEvent.Edit.Type.Name -> stringResource(R.string.profile_data_realName)
            IntroduceEvent.Edit.Type.NickName -> stringResource(R.string.profile_data_name)
        },
        content = {
            
        },
        modifier = modifier
    )
}
