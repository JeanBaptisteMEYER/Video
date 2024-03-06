package com.jbm.module.core.model

import com.jbm.module.core.data.model.VideoDTO
import com.jbm.module.core.database.model.VideoEntity
import kotlinx.datetime.Clock

data class VideoDomain(
    val id: String,
    val name: String,
    val videoUrl: String,
    val downloadState: VideoDownloadState
)

fun VideoDomain.toDTO(): VideoDTO =
    VideoDTO(
        id = id,
        title = name,
        videoUrl = listOf(videoUrl),
        description = "",
        thumb = "",
        subtitle = ""
    )

fun VideoDomain.toEntity(): VideoEntity =
    VideoEntity(
        id = id.toInt(),
        title = name,
        videoUrl = videoUrl,
        downloadState = downloadState.value,
        description = "",
        thumb = "",
        subtitle = "",
        updatedAt = Clock.System.now()
    )

