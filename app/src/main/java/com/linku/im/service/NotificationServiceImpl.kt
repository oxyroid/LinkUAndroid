package com.linku.im.service

import android.content.Context
import android.media.SoundPool
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.linku.domain.entity.Message
import com.linku.domain.service.NotificationService
import com.linku.im.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationServiceImpl @Inject constructor(
    private val soundPool: SoundPool,
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat,
    @ApplicationContext private val context: Context
) : NotificationService {
    override fun onCollected(message: Message) {
        soundPool.load(context, R.raw.sound_in, 1)
        val notification = notificationBuilder
            .setContentText(message.content)
            .build()
        notificationManager.notify(message.cid, notification)
    }

    override fun onEmit() {
        soundPool.load(context, R.raw.sound_out, 1)
        notificationManager.notify(-1, notificationBuilder.build())
    }
}