package com.linku.fs_core.fs

import android.net.Uri
import java.io.File

interface ReadFileScheme : FileScheme {
    fun get(uri: Uri): File?
    fun cached(): List<File>
    fun decrypt(): String
}

fun ReadFileScheme.getOrDefault(uri: Uri, default: File?): File? = get(uri) ?: default
fun ReadFileScheme.getOrNull(uri: Uri): File? = getOrDefault(uri, null)