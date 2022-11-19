package com.linku.im.screen.setting.notification

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.im.R
import com.linku.im.screen.setting.BasicSettingScreen
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.screen.setting.common.CheckBoxItem

@Composable
fun NotificationSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    BasicSettingScreen(
        title = stringResource(R.string.profile_settings_notification),
        content = {
            CheckBoxItem(
                title = "原生的底部通知栏",
                enabled = true,
                checked = state.isNativeSnackBar,
                onCheckedChange = { viewModel.onEvent(SettingEvent.Notification.OnNativeSnackBar) }
            )
        },
        modifier = modifier
    )
}
