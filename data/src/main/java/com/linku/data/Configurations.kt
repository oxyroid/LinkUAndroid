package com.linku.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import com.linku.core.extension.delegates.boolean
import com.linku.core.extension.delegates.int
import com.linku.core.extension.delegates.string
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Configurations @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)

    var currentUID: Int by sharedPreferences.int(-1)
    var token: String? by sharedPreferences.string()

    var isLogMode: Boolean by sharedPreferences.boolean(false)

    var isDarkMode: Boolean by sharedPreferences.boolean(false) { newValue ->
        isDarkModeSubscribers.forEach { it.onChanged(newValue) }
    }
    var lightTheme: Int by sharedPreferences.int(-1)
    var darkTheme: Int by sharedPreferences.int(-1)
    var isNativeSnackBar: Boolean by sharedPreferences.boolean(true) { newValue ->
        isNativeSnackBarSubscribers.forEach { it.onChanged(newValue) }
    }

    private val isDarkModeSubscribers = mutableSetOf<Observer<Boolean>>()
    private val isNativeSnackBarSubscribers = mutableSetOf<Observer<Boolean>>()

    fun subscribeIsDarkMode(observer: Observer<Boolean>) {
        isDarkModeSubscribers.add(observer)
    }

    fun subscribeIsNativeSnackBar(observer: Observer<Boolean>) {
        isNativeSnackBarSubscribers.add(observer)
    }

    inline fun log(block: () -> Unit) {
        if (isLogMode) block()
    }

    companion object {
        private const val SHARED_SETTINGS = "shared_settings"
    }
}
