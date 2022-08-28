package com.linku.fs_core.fs

import android.net.Uri
import androidx.core.net.toUri
import java.io.File

interface WriteFileScheme : FileScheme {
    fun put(uri: Uri?): File?
}

fun WriteFileScheme.cache(uri: Uri?): Uri? {
    return put(uri)?.toUri()
}