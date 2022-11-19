package com.linku.im.screen.setting.notification

import com.linku.data.Configurations
import com.linku.im.LinkUEvent
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.screen.setting.SettingState
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val configurations: Configurations,
) : BaseViewModel<SettingState.Notification, SettingEvent.Notification>(SettingState.Notification()) {
    init {
        configurations.subscribeIsNativeSnackBar {
            writable = readable.copy(
                isNativeSnackBar = it
            )
        }
    }

    override fun onEvent(event: SettingEvent.Notification) {
        when (event) {
            SettingEvent.Notification.OnNativeSnackBar -> {
                val target = !configurations.isNativeSnackBar
                vm.onEvent(LinkUEvent.OnNativeSnackBar(target))
                configurations.isNativeSnackBar = target
            }
        }
    }
}
