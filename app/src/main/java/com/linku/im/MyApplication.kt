package com.linku.im

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.notification
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

@HiltAndroidApp
class MyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MMKV.initialize(this)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            notification {
                title = getString(R.string.crash_title)
                text = getString(R.string.crash_text)
                channelName = getString(R.string.crash_channel)
            }
        }
    }
}