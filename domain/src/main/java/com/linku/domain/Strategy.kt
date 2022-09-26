package com.linku.domain

sealed class Strategy {
    object Memory : Strategy()
    object OnlyCache : Strategy()
    object OnlyNetwork : Strategy()
    object NetworkElseCache : Strategy()
    object CacheElseNetwork : Strategy()
}