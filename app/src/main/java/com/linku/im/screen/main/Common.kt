package com.linku.im.screen.main

import androidx.annotation.IntRange

sealed class PageConfig {
    companion object {
        fun parse(
            isCommon: Boolean = true,
            @IntRange(from = 0, to = 4) index: Int
        ): PageConfig = if (isCommon) {
            when (index) {
                0 -> Common.Main
                1 -> Common.Conversation
                2 -> Common.Contract
                3 -> Common.More
                else -> throw IndexOutOfBoundsException()
            }
        } else {
            when (index) {
                0, 1 -> Query.Message
                2 -> Query.Conversation
                3 -> Query.User
                else -> throw IndexOutOfBoundsException()
            }
        }
    }

    sealed class Common : PageConfig() {
        object Main : Common()
        object Conversation : Common()
        object Contract : Common()
        object More : Common()
    }

    sealed class Query : PageConfig() {
        object Conversation : Query()
        object User : Query()
        object Message : Query()
    }

}