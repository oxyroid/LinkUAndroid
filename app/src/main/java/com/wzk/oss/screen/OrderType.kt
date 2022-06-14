package com.wzk.oss.screen

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}