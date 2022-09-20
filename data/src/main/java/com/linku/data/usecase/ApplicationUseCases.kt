package com.linku.data.usecase

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class ApplicationUseCases @Inject constructor(
    val getString: GetStringUseCase,
    val toast: ToastUseCase,
    val getSystemService: GetSystemServiceUseCase
)

data class GetSystemServiceUseCase @Inject constructor(
    @ApplicationContext val context: Context
) {
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T> invoke(): T {
        return context.getSystemService(T::class.java) as T
    }
}

data class GetStringUseCase @Inject constructor(
    @ApplicationContext val context: Context
) {
    operator fun invoke(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}

data class ToastUseCase @Inject constructor(
    @ApplicationContext val context: Context
) {
    operator fun invoke(text: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }
}