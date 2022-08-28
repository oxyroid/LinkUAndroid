package com.linku.fs_android

import android.util.Log
import com.linku.fs_core.logger.Logger

object AndroidLogger : Logger {
    override fun log(s: String) {
        Log.d(TAG, s)
    }

    override fun warn(s: String) {
        Log.w(TAG, "warn: $s")
    }

    override fun error(s: String, thenThrow: Boolean) {
        Log.e(TAG, "error: $s")
        if (thenThrow) throw RuntimeException(s)
    }


    private const val TAG = "FileScheme"
}