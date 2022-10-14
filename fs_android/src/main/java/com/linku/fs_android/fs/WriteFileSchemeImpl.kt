package com.linku.fs_android.fs

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.linku.fs_android.util.Resolver
import com.linku.fs_core.crypto.CryptoManager
import com.linku.fs_core.fs.WriteFileScheme
import com.linku.fs_core.logger.Logger
import java.io.File
import java.nio.file.Files

class WriteFileSchemeImpl(
    private val context: Context,
    logger: Logger,
    private val resolver: Resolver,
    private val cryptoManager: CryptoManager
) : WriteFileScheme {
    override fun put(uri: Uri?, fixedFormat: String?): File? = run {
        uri ?: return null
        try {
            when (uri.scheme) {
                SCHEME_CONTENT -> {
                    val display = resolver.getDisplay(uri)
                    val fileName = if (display != null) "display.$fixedFormat" else return@run null
                    val file = File(context.externalCacheDir, fileName)
                    if (file.exists()) file.delete()
                    val resolver = context.contentResolver
                    resolver.openInputStream(uri).use { input ->
                        Files.copy(input, file.toPath())
                    }
                    file.createNewFile()
                    file
                }
                SCHEME_FILE -> uri.toFile()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun encrypt(text: String): String {
        val bytes = text.encodeToByteArray()
        val file = File(context.filesDir, "secret.txt")
        if (!file.exists()) file.createNewFile()
        return file.outputStream().use {
            cryptoManager.encrypt(bytes, it).decodeToString()
        }
    }
}
