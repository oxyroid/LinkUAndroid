package com.linku.domain.extension.delegates

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private inline fun <T> SharedPreferences.delegate(
    key: String? = null,
    defaultValue: T,
    crossinline getter: SharedPreferences.(String, T) -> T,
    crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
            getter(key ?: property.name, defaultValue)

        override fun setValue(
            thisRef: Any, property: KProperty<*>,
            value: T
        ) = edit().setter(key ?: property.name, value).apply()
    }
}

fun SharedPreferences.int(def: Int = 0, key: String? = null) =
    delegate(key, def, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

fun SharedPreferences.string(def: String, key: String? = null) =
    delegate(key, def, SharedPreferences::getString, SharedPreferences.Editor::putString)

fun SharedPreferences.nullableString(def: String? = null, key: String? = null) =
    delegate(key, def, SharedPreferences::getString, SharedPreferences.Editor::putString)

fun SharedPreferences.nullableInt(def: Int? = null, key: String? = null) =
    delegate(
        key,
        def,
        { k, v -> getString(k, v?.toString())?.toInt() },
        { k, v -> putString(k, v.toString()) }
    )

fun SharedPreferences.long(def: Long = 0, key: String? = null) =
    delegate(key, def, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

fun SharedPreferences.boolean(def: Boolean = false, key: String? = null) =
    delegate(key, def, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)
