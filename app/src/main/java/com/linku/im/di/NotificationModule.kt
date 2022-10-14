package com.linku.im.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.linku.im.Constants
import com.linku.im.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    @Provides
    @Singleton
    fun provideSoundPool(): SoundPool {
        val attributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .build()
        return SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()
            .also {
                it.setOnLoadCompleteListener { soundPool, sampleId, status ->
                    if (status == 0) {
                        soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val manager = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(
            Constants.NOTIFICATION_ID,
            "Messages",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
        return manager
    }


    @Provides
    @Singleton
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_ID)
            .setContentTitle("Welcome")
            .setContentText("This is content text")
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


}
