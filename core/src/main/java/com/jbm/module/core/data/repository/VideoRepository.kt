package com.jbm.module.core.data.repository

import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    suspend fun getVideoLibrary(): List<VideoDomain>
    suspend fun getVideoById(id: String): VideoDomain
    suspend fun downloadVideo(video: VideoDomain): Flow<VideoDownloadState>
    suspend fun deleteDownloadedVideoById(videoId: String): Flow<VideoDownloadState>
}
