package com.jbm.module.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import kotlinx.datetime.Instant

/**
 * [downloadState] is based on androidx.media3.exoplayer.offline.Download STATE_*
 * STATE_QUEUED = 0
 * STATE_STOPPED = 1
 * STATE_DOWNLOADING = 2
 * STATE_COMPLETED = 3
 * STATE_FAILED = 4
 * STATE_REMOVING = 5
 * STATE_RESTARTING = 7
 */

@Entity(tableName = "video")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 1,
    val title: String,
    val subtitle: String,
    val description: String,
    val videoUrl: String,
    val thumb: String,
    val downloadState: Int,
    val updatedAt: Instant
)

fun VideoEntity.toDomain(): VideoDomain =
    VideoDomain(
        id.toString(),
        title,
        videoUrl,
        VideoDownloadState.entries.firstOrNull { it.value == downloadState }
            ?: VideoDownloadState.Unknown
    )

fun List<VideoEntity>.toDomain(): List<VideoDomain> = map { it.toDomain() }
