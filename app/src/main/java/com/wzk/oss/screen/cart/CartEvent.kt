package com.wzk.oss.screen.cart

sealed class CartEvent {
    object LoadAll : CartEvent()
}
