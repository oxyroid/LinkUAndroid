package com.linku.fs_android.fs

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import com.linku.fs_android.util.Resolver
import com.linku.fs_core.fs.WriteFileScheme
import com.linku.fs_core.logger.Logger
import java.io.File
import java.nio.file.Files
import java.util.*

@RequiresApi(Build.VERSION_CODES.R)
class WriteFileSchemeImpl30(
    private val context: Context,
    logger: Logger,
    private val resolver: Resolver
) : WriteFileScheme {
    override fun put(uri: Uri?): File? {
        uri ?: return null
        val s = uri.toString()
        return try {
            when {
                s.startsWith(SCHEME_CONTENT) -> {
                    val fileName = resolver.getDisplay(uri) ?: return null
                    val file = File(context.externalCacheDir, fileName)
                    if (file.exists()) file.delete()
                    val resolver = context.contentResolver
                    resolver.openInputStream(uri).use { input ->
                        Files.copy(input, file.toPath())
                    }
                    file
                }
                s.startsWith(SCHEME_FILE) -> {
                    val file = uri.toFile()
                    File(context.externalCacheDir, file.name)
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}