package com.linku.fs_android.fs

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.linku.fs_core.fs.ReadFileScheme
import com.linku.fs_core.logger.Logger
import java.io.File

open class ReadFileSchemeImpl(
    private val context: Context,
    private val logger: Logger
) : ReadFileScheme {
    override fun get(uri: Uri): File? {
        val s = uri.toString()
        return when {
            s.startsWith(SCHEME_FILE) -> uri.toFile()
            s.startsWith(SCHEME_CONTENT) -> {
                logger.error("Scheme: ${uri.scheme} should be delivered in WriteFileScheme.", true)
                null
            }
            else -> {
                logger.warn("The scheme: ${uri.scheme} is not supported.")
                null
            }
        }
    }

    override fun cached(): List<File> {
        return context.externalCacheDir?.listFiles()?.toList() ?: emptyList()
    }
}