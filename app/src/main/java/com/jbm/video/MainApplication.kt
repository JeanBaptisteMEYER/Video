package com.jbm.video

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.jbm.module.core.video.VideoDownloadService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            VideoDownloadService.NOTIFICATION_CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = getString(com.jbm.module.core.R.string.notification_desc)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
