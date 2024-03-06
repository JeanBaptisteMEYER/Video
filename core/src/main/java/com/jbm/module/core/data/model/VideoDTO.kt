package com.jbm.module.core.data.model

import com.google.gson.annotations.SerializedName
import com.jbm.module.core.database.model.VideoEntity
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import kotlinx.datetime.Clock

data class VideoListDTO(
    @SerializedName("videos") val videoList: List<VideoDTO>
)

data class VideoDTO(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String,
    @SerializedName("sources") val videoUrl: List<String>,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("thumb") val thumb: String,
    @SerializedName("title") val title: String,
)

fun VideoDTO.toDomain(): VideoDomain =
    VideoDomain(
        id,
        title,
        videoUrl.firstOrNull() ?: "",
        VideoDownloadState.Unknown
    )

fun List<VideoDTO>.toDomain(): List<VideoDomain> = map { it.toDomain() }

fun VideoDTO.toEntity(): VideoEntity =
    VideoEntity(
        id = id.toInt(),
        description = description,
        videoUrl = videoUrl.firstOrNull() ?: "",
        subtitle = subtitle,
        thumb = thumb,
        title = title,
        downloadState = VideoDownloadState.Unknown.value,
        updatedAt = Clock.System.now()
    )
