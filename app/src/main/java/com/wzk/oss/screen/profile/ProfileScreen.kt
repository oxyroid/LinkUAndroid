package com.wzk.oss.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.wzk.oss.ui.MaterialIconButton
import com.wzk.oss.ui.MaterialSnackHost
import com.wzk.oss.ui.MaterialTopBar
import com.wzk.oss.extension.times
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.profile.composable.AccountItemGroup
import com.wzk.oss.screen.profile.composable.Setting

@Composable
fun AccountScreen(
    navController: NavController
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            MaterialSnackHost(state = scaffoldState.snackbarHostState)
        },
        topBar = {
            MaterialTopBar(
                onNavClick = navController::popBackStack,
                actions = {
                    MaterialIconButton(
                        icon = Icons.Default.MoreVert,
                        onClick = {

                        },
                    )
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
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
                AccountItemGroup(
                    label = "账号", items = listOf(
                        Setting.Entity(
                            key = "手机号码",
                            value = "+86 176-2297-1229",
                        ) {

                        },
                        Setting.Entity(
                            key = "用户名",
                            value = "@sortBy",
                        ) {

                        },
                        Setting.Entity(
                            key = "个人简介",
                            value = "TG Space -> @earthBlock",
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
                        .background(MaterialTheme.colorScheme.outline * 0.25f)
                )
            }
            item {
                AccountItemGroup(
                    label = "设置", items = listOf(
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
                        ),
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
                        ),
                    )
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outline * 0.25f)
                )
            }
        }
    }

}

