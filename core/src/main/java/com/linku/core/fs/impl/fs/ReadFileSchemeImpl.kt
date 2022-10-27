package com.linku.core.fs.impl.fs

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import java.io.File

class ReadFileSchemeImpl(
    private val context: Context,
    private val logger: com.linku.core.fs.logger.Logger,
    private val cryptoManager: com.linku.core.fs.crypto.CryptoManager
) : com.linku.core.fs.fs.ReadFileScheme {
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

    override fun decrypt(): String = cryptoManager
        .decrypt(File(context.filesDir, "secret.txt").inputStream())
        .decodeToString()

}