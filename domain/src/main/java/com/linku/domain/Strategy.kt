package com.linku.domain

sealed class Strategy {
    object Memory : Strategy()
    object OnlyCache : Strategy()
    object OnlyNetwork : Strategy()
    object NetworkThenCache : Strategy()
    object CacheElseNetwork : Strategy()
}