package com.linku.domain.extension

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class IntPreference(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Int? = null
) : ReadWriteProperty<Any, Int?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int? {
        return sharedPreferences.getString(key, defaultValue?.toString())?.toInt()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int?) {
        sharedPreferences.edit {
            putString(key, value?.toString())
        }
    }
}

class StringPreference(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String? = null
) : ReadWriteProperty<Any, String?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }
}

class BooleanPreference(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }
}
