package com.linku.data.usecase

import android.content.Context
import android.content.SharedPreferences
import com.linku.domain.extension.delegates.boolean
import com.linku.domain.extension.delegates.int
import com.linku.domain.extension.delegates.nullableInt
import com.linku.domain.extension.delegates.nullableString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SharedPreferenceUseCase @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
    var isDarkMode: Boolean by sharedPreferences.boolean(false)
    var isDynamicMode: Boolean by sharedPreferences.boolean(false)
    var currentUID: Int? by sharedPreferences.nullableInt()
    var token: String? by sharedPreferences.nullableString()
    var isLogMode: Boolean by sharedPreferences.boolean(false)

    var lightTheme: Int by sharedPreferences.int(-1)
    var darkTheme: Int by sharedPreferences.int(-1)

    inline fun log(block: () -> Unit) {
        if (isLogMode) block()
    }

    companion object {
        private const val SHARED_SETTINGS = "shared_settings"
    }
}
