package com.wzk.oss.screen.main.composable

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.wzk.oss.screen.Screen

data class MainDrawerItemDTO(
    @StringRes val titleRes: Int,
    val screen: Screen,
    val icon: ImageVector
)
