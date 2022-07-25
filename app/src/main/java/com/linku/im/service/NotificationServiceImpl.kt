package com.linku.im.service

import android.content.Context
import android.media.SoundPool
import com.linku.domain.entity.Message
import com.linku.domain.service.NotificationService
import com.linku.im.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationServiceImpl @Inject constructor(
    private val soundPool: SoundPool,
    @ApplicationContext private val context: Context
) : NotificationService {
    override fun onReceived(message: Message) {
        soundPool.load(context, R.raw.sound_in, 1)
    }

    override fun onEmit() {
        soundPool.load(context, R.raw.sound_out, 1)
    }
}