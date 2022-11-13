package com.linku.im.screen.main

import androidx.annotation.IntRange

sealed class PageConfig {
    companion object {
        fun parse(
            isCommon: Boolean = true,
            @IntRange(from = 0, to = 3) index: Int
        ): PageConfig = if (isCommon) {
            when (index) {
                0 -> Common.Conversation
                1 -> Common.Contract
                2 -> Common.More
                else -> throw PageOutOfRangeException
            }
        } else {
            when (index) {
                0 -> Query.Message
                1 -> Query.Conversation
                2 -> Query.User
                else -> throw PageOutOfRangeException
            }
        }
    }

    sealed class Common : PageConfig() {
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


internal object PageOutOfRangeException : Exception()
