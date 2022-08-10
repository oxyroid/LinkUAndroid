package com.linku.data.usecase

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext

data class ApplicationUseCases(
    val getString: GetStringUseCase,
    val toast: ToastUseCase,
    val getSystemService: GetSystemServiceUseCase
)

data class GetSystemServiceUseCase(
    @ApplicationContext val context: Context
) {
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T> invoke(): T {
        return context.getSystemService(T::class.java) as T
    }
}

data class GetStringUseCase(
    @ApplicationContext val context: Context
) {
    operator fun invoke(resId: Int): String {
        return context.getString(resId)
    }
}

data class ToastUseCase(
    @ApplicationContext val context: Context
) {
    operator fun invoke(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }
}