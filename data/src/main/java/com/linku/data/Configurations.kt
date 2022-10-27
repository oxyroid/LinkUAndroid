package com.linku.data

import android.content.Context
import android.content.SharedPreferences
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

    var isDarkMode: Boolean by sharedPreferences.boolean(false)
    var lightTheme: Int by sharedPreferences.int(-1)
    var darkTheme: Int by sharedPreferences.int(-1)

    inline fun log(block: () -> Unit) {
        if (isLogMode) block()
    }

    companion object {
        private const val SHARED_SETTINGS = "shared_settings"
    }
}
