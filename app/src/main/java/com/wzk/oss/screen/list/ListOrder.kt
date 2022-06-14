package com.wzk.oss.screen.list

import com.wzk.oss.screen.OrderType

sealed class ListOrder {
    data class Name(
        val orderType: OrderType = OrderType.Ascending
    ) : ListOrder()

    data class Price(
        val orderType: OrderType = OrderType.Ascending
    ) : ListOrder()
}