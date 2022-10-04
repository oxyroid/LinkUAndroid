package com.linku.im

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.mailSender
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            toast {
                text = getString(R.string.crash_text)
            }
            mailSender {
                mailTo = "1365427005@qq.com"
                reportAsFile = true
                reportFileName = "crash_${System.currentTimeMillis()}.txt"
                subject = "LinkU Android Crash"
                body = ""
            }
        }
    }
}