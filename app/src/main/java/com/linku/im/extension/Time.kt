package com.linku.im.extension

import java.util.*

val Long.friendlyFormatted
    get() = run {
        val calendar = Calendar.getInstance()
        calendar.time = Date(this)
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
            .let {
                if (it.length < 2)  "0$it"
                else it
            }
        val minute = calendar.get(Calendar.MINUTE).toString()
            .let {
                if (it.length < 2)  "0$it"
                else it
            }
        "$hour:$minute"
    }