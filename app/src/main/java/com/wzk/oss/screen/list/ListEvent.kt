package com.wzk.oss.screen.list

import androidx.annotation.StringRes
import com.wzk.oss.screen.list.composable.MenuItem

sealed class ListEvent {
    data class Fetch(val listOrder: ListOrder) : ListEvent()
    data class Order(val listOrder: ListOrder) : ListEvent()
}

operator fun ListEvent.Order.plus(@StringRes resId: Int): MenuItem = MenuItem(resId, this)