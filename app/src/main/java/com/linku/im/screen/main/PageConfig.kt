package com.linku.im.screen.main

import androidx.annotation.IntRange

sealed class PageConfig {
    object Conversation : PageConfig()
    object Contract : PageConfig()
    object More : PageConfig()
    companion object {
        fun parse(@IntRange(from = 0, to = 2) index: Int) = when (index) {
            0 -> Conversation
            1 -> Contract
            2 -> More
            else -> throw PageOutOfRangeException
        }
    }
}

internal object PageOutOfRangeException : Exception()