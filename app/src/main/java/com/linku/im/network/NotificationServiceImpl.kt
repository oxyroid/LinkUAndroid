package com.linku.im.network

import android.Manifest
import android.content.Context
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCollected(message: Message) {
        soundPool.load(context, R.raw.sound_in, 1)
        val content = when (message) {
            is TextMessage -> message.text
            is ImageMessage -> getString(R.string.image_message)
            is GraphicsMessage -> getString(R.string.graphics_message)
            else -> getString(R.string.unknown_message_type)
        }
        val notification = notificationBuilder
            .setContentText(content)
            .build()
        notificationManager.notify(message.cid, notification)
    }

    override fun onEmit() {
        soundPool.load(context, R.raw.sound_out, 1)
    }

    private fun getString(resId: Int): String = context.getString(resId)
}
