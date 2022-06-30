package com.linku.im.screen

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}