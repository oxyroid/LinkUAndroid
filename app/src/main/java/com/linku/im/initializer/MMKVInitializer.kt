@file:Suppress("unused")
package com.linku.im.initializer

import android.content.Context
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV

class MMKVInitializer : Initializer<MMKV> {
    override fun create(context: Context): MMKV {
        MMKV.initialize(context)
        return MMKV.defaultMMKV()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
