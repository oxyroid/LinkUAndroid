package com.linku.core.fs.logger

interface Logger {
    fun log(s: String)
    fun warn(s: String)
    fun error(s: String, thenThrow: Boolean = false)
}