package com.jbm.module.core.network.video.download

import com.jbm.module.core.data.model.VideoDTO
import com.jbm.module.core.model.VideoDomain
import kotlinx.coroutines.flow.Flow

interface VideoDownloadDataSource {
    suspend fun downloadVideo(video: VideoDTO): Flow<Int>
    suspend fun deleteDownloadedVideoById(videoId: String): Flow<Int>
    suspend fun getDownloadedVideoList(): List<VideoDomain>
}
