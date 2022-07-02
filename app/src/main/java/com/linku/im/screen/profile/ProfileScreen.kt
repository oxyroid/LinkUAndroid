package com.linku.im.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.linku.im.NavViewModel
import com.linku.im.extension.debug
import com.linku.im.screen.Screen
import com.linku.im.screen.profile.composable.ProfileItems
import com.linku.im.screen.profile.composable.Setting


val itemsIntro = listOf(
    Setting.Entity(
        key = "手机号码",
        value = "+86 130-0000-0000",
    ) {

    },
    Setting.Entity(
        key = "用户名",
        value = "@test",
    ) {

    },
    Setting.Entity(
        key = "个人简介",
        value = "测试数据",
    ) {

    },
)

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
    navController: NavController,
    navViewModel: NavViewModel
) {

    with(navViewModel) {
        rememberedIcon.value = Icons.Default.ArrowBack
        rememberedTitle.value = ""
        rememberedOnNavClick.value = {
            navController.popBackStack()
        }
        rememberedActions.value = {
            IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
            }
        }
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
                label = "账号", items = debug { itemsIntro } ?: emptyList()
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
    }
}

