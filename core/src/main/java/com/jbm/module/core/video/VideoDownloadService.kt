package com.jbm.module.core.video

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.jbm.module.core.R
import com.jbm.module.core.video.di.VideoDownloadServiceEntryPoint
import dagger.hilt.EntryPoints
import javax.inject.Inject

@OptIn(UnstableApi::class)
class VideoDownloadService @Inject constructor() : DownloadService(
    1,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    NOTIFICATION_CHANNEL_ID,
    R.string.notification_app_name,
    R.string.notification_desc
) {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "video_download_service_channel_id"
    }

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationHelper: DownloadNotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = DownloadNotificationHelper(this, NOTIFICATION_CHANNEL_ID)
    }

    override fun getDownloadManager(): DownloadManager {
        downloadManager = EntryPoints.get(
            applicationContext,
            VideoDownloadServiceEntryPoint::class.java
        ).getDownloadManager()

        //Set the maximum number of parallel downloads
        downloadManager.maxParallelDownloads = 5

        return downloadManager
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return notificationHelper.buildProgressNotification(
            this@VideoDownloadService,
            R.drawable.ic_downloading,
            null,
            getString(R.string.notification_desc),
            downloads,
            notMetRequirements
        )
    }
}
