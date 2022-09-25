package com.linku.im

data class LinkUState(
    val loading: Boolean = false,
    val label: String? = null,
    val isDarkMode: Boolean = false,
    val isEmojiReady: Boolean = false,
    val hasSynced: Boolean = false,
)
