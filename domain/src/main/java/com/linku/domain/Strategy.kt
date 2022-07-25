package com.linku.domain

sealed class Strategy {
    object CacheElseNetwork : Strategy()
    object NetworkThenCache : Strategy()
    object OnlyCache : Strategy()
    object OnlyNetwork : Strategy()
    object Memory : Strategy()
}