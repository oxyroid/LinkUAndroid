package com.linku.im

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp


val application get() = MyApplication._application

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        lateinit var _application: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        _application = this
        MMKV.initialize(this)
    }
}