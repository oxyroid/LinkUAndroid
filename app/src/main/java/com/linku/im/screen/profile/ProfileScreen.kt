package com.linku.im.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.linku.im.R
import com.linku.im.extension.debug
import com.linku.im.overall
import com.linku.im.screen.Screen
import com.linku.im.screen.overall.OverallEvent
import com.linku.im.screen.profile.composable.ProfileItems
import com.linku.im.screen.profile.composable.Setting
import com.linku.im.ui.MaterialButton


val itemsFolder = listOf(
    Setting.Folder(
        label = "通知和声音",
        icon = Icons.Default.Notifications,
        screen = Screen.ProfileScreen
    ),
    Setting.Folder(
        label = "隐私和安全",
        icon = Icons.Default.Lock,
        screen = Screen.ProfileScreen
    ),
    Setting.Folder(
        label = "数据和存储",
        icon = Icons.Default.DateRange,
        screen = Screen.ProfileScreen
    )
)

@Composable
fun AccountScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.FetchProfile)
    }
    LaunchedEffect(state.logout) {
        if (state.logout)
            overall.onEvent(OverallEvent.PopBackStack)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                SubcomposeAsyncImage(
                    model = Color.Blue,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4 / 3f)
                )
            }
        }
        item {
            ProfileItems(
                label = "账号",
                items = listOf(
                    Setting.Entity(
                        key = "电子邮箱",
                        value = state.email,
                    ) {

                    },
                    Setting.Entity(
                        key = "用户名",
                        value = state.name,
                    ) {

                    },
                    Setting.Entity(
                        key = "真实姓名",
                        value = state.realName,
                    ) {

                    },
                    Setting.Entity(
                        key = "个人简介",
                        value = "测试数据",
                    ) {

                    },
                )
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth()
            )
        }
        item {
            ProfileItems(
                label = "设置", items = debug {
                    listOf(
                        Setting.Folder(
                            label = "测试反馈",
                            icon = Icons.Default.Settings,
                            screen = Screen.ProfileScreen
                        )
                    )
                } ?: itemsFolder
            )
        }

        item {
            Surface {
                MaterialButton(textRes = R.string.logout) {
                    viewModel.onEvent(ProfileEvent.SignOut)
                }
            }
        }
    }
}

