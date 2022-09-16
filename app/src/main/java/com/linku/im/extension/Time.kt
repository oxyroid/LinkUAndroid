package com.linku.im.extension

import java.util.*

val Long.friendlyFormatted
    get() = run {
        val calendar = Calendar.getInstance()
        calendar.time = Date(this)
        isToday.ifTrue {
            val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
                .let {
                    if (it.length < 2) "0$it"
                    else it
                }
            val minute = calendar.get(Calendar.MINUTE).toString()
                .let {
                    if (it.length < 2) "0$it"
                    else it
                }
            "$hour:$minute"
        } ?: run {
            val month = (calendar.get(Calendar.MONTH) + 1).toString()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                .let {
                    if (it.length < 2) "0$it"
                    else it
                }
            "$month-$day"
        }
    }
val Long.isToday: Boolean
    get() = run {
        val current = Calendar.getInstance()
            .apply {
                time = Date(this@isToday)
            }
        val todayBegin = Calendar.getInstance()
            .apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MILLISECOND, 0)
            }
        current.equals(todayBegin) || current.after(todayBegin)
    }

fun Long.isSameDay(another: Long): Boolean = kotlin.run {
    val calendar = Calendar.getInstance().apply {
        time = Date(this@isSameDay)
    }
    val anotherCalendar = Calendar.getInstance().apply {
        time = Date(another)
    }
    calendar.get(Calendar.DAY_OF_YEAR) == anotherCalendar.get(Calendar.DAY_OF_YEAR)
}