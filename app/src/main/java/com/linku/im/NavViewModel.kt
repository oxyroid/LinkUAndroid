package com.linku.im

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tencent.mmkv.MMKV

class NavViewModel : ViewModel() {

    var rememberedIcon = mutableStateOf(Icons.Default.Menu)

    var rememberedTitle = mutableStateOf("")

    var rememberedOnNavClick = mutableStateOf({})

    var rememberedActions = mutableStateOf<@Composable RowScope.() -> Unit>(@Composable { })

    var isDarkMode = mutableStateOf(
        MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
    )

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        const val SAVED_DARK_MODE = "saved:dark-mode"
    }

}