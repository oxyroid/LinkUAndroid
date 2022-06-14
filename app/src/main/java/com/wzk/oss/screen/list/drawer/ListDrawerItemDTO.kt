package com.wzk.oss.screen.list.drawer

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.wzk.oss.screen.Screen

data class ListDrawerItemDTO(
    @StringRes val titleRes: Int,
    val screen: Screen,
    val icon: ImageVector
)
