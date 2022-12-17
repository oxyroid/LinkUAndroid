package com.linku.data.usecase

import android.content.ContentResolver
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * This toolset cannot reference references other than [Context].
 */
data class ApplicationUseCases @Inject constructor(
    val getString: GetStringUseCase,
    val getSystemService: GetSystemServiceUseCase,
    val contentResolver: ContentResolverUseCase
)

data class ContentResolverUseCase @Inject constructor(
    @ApplicationContext val context: Context
) {
    operator fun invoke(): ContentResolver {
        return context.contentResolver
    }
}

data class GetSystemServiceUseCase @Inject constructor(
    @ApplicationContext val context: Context
) {
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
