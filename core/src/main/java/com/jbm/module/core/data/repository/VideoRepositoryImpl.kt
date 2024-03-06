package com.jbm.module.core.data.repository

import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.database.dao.VideoDao
import com.jbm.module.core.database.model.toDomain
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import com.jbm.module.core.network.video.library.VideoLibraryDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val videoLibraryDataSource: VideoLibraryDataSource,
    private val videoDownloadDataSource: VideoDownloadDataSource,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoRepository {
    override suspend fun getVideoLibrary(): List<VideoDomain> =
        videoLibraryDataSource.getVideoLibrary()

    override suspend fun getVideoById(id: String): VideoDomain =
        withContext(dispatcherIO) {
            return@withContext videoDao.getById(id).toDomain()
        }

    override suspend fun downloadVideo(video: VideoDomain): Flow<VideoDownloadState> =
        videoDownloadDataSource.downloadVideo(video)

    override suspend fun deleteDownloadedVideoById(videoId: String): Flow<VideoDownloadState> =
        videoDownloadDataSource.deleteDownloadedVideoById(videoId)
}
