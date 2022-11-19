package com.linku.domain.repository

import kotlin.reflect.KProperty

sealed class MimeType(val value: String) {
    object TestPlain : MimeType("text/plain")
}

operator fun MimeType.getValue(thisRef: Any?, property: KProperty<*>): String = value
