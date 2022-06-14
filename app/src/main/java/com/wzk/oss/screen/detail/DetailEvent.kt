package com.wzk.oss.screen.detail

sealed class DetailEvent {
    data class Find(val id: Int) : DetailEvent()
    data class AddToCart(val id: Int) : DetailEvent()
}
