package com.linku.core.fs.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

class Resolver(
    private val context: Context
) {
    fun getDisplay(uri: Uri?): String? {
        uri ?: return null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        return context.contentResolver.query(
            uri, projection, null, null, null
        ).use { cursor ->
            cursor?.let {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else null
            }
        }
    }
}
