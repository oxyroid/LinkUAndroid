package com.wzk.oss

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp


val application get() = OssApplication._application

@HiltAndroidApp
class OssApplication : Application() {
    companion object {
        internal lateinit var _application: OssApplication
    }

    override fun onCreate() {
        super.onCreate()
        _application = this
        MMKV.initialize(this)
    }
}