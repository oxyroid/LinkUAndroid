package com.linku.fs_android.fs

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.linku.fs_android.util.Resolver
import com.linku.fs_core.fs.WriteFileScheme
import com.linku.fs_core.logger.Logger
import java.io.File

class WriteFileSchemeImpl(
    private val context: Context,
    logger: Logger,
    private val resolver: Resolver
) : WriteFileScheme {
    override fun put(uri: Uri?): File? {
        uri ?: return null
        val s = uri.toString()
        return when {
            s.startsWith(SCHEME_CONTENT) -> {
                val fileName = resolver.getDisplay(uri) ?: return null
                val file = File(context.externalCacheDir, fileName)
                file.createNewFile()
                file.outputStream().use {
                    context.contentResolver.openInputStream(uri).use { stream ->
                        stream?.copyTo(it)
                    }
                }
                file
            }
            s.startsWith(SCHEME_FILE) -> {
                val file = uri.toFile()
                File(context.externalCacheDir, file.name)
            }
            else -> null
        }
    }
}