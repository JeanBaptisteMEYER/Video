package com.jbm.module.core.data.repository

import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.data.model.toDomain
import com.jbm.module.core.data.model.toEntity
import com.jbm.module.core.database.dao.VideoDao
import com.jbm.module.core.database.model.toDomain
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import com.jbm.module.core.model.toDTO
import com.jbm.module.core.model.toEntity
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import com.jbm.module.core.network.video.library.VideoLibraryDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val videoLibraryDataSource: VideoLibraryDataSource,
    private val videoDownloadDataSource: VideoDownloadDataSource,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoRepository {
    override suspend fun getVideoLibrary(): List<VideoDomain> =
        videoLibraryDataSource.getVideoLibrary().toDomain()

    override suspend fun getVideoLibraryAsFlow(): Flow<List<VideoDomain>> =
        videoDao.getAll().map { it.toDomain() }

    override suspend fun downloadAndSaveLibrary() {
        withContext(dispatcherIO) {
            videoLibraryDataSource.getVideoLibrary().forEach {
                videoDao.insert(it.toEntity())
            }
        }
    }

    override suspend fun getVideoById(id: String): VideoDomain =
        withContext(dispatcherIO) {
            return@withContext videoDao.getById(id).toDomain()
        }

    override suspend fun downloadVideo(video: VideoDomain) {
        withContext(dispatcherIO) {
            videoDownloadDataSource.downloadVideo(video.toDTO())
                .collect { videoDownloadState ->
                    videoDao.insert(
                        video.copy(
                            downloadState = VideoDownloadState.entries.firstOrNull { it.value == videoDownloadState }
                                ?: VideoDownloadState.Unknown
                        ).toEntity()
                    )
                }
        }
    }

    override suspend fun deleteDownloadedVideoById(video: VideoDomain) {
        withContext(dispatcherIO) {
            videoDownloadDataSource.deleteDownloadedVideoById(video.id)
                .collect { videoDownloadState ->
                    if (videoDownloadState == VideoDownloadState.Removing.value) {
                        videoDao.insert(
                            video.copy(
                                downloadState = VideoDownloadState.Unknown
                            ).toEntity()
                        )
                    }
                }
        }
    }
}
