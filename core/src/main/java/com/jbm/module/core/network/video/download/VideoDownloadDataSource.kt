package com.jbm.module.core.network.video.download

import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import kotlinx.coroutines.flow.Flow

interface VideoDownloadDataSource {
    suspend fun downloadVideo(video: VideoDomain): Flow<VideoDownloadState>
    suspend fun deleteDownloadedVideoById(videoId: String): Flow<VideoDownloadState>
    suspend fun getDownloadedVideoList(): List<VideoDomain>
}
