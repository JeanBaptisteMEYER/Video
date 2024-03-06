package com.jbm.module.core.network.video.library

import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoLibraryDataSourceImpl @Inject constructor(
    private val videoDownloadDataSource: VideoDownloadDataSource,
    private val offlineVideoLibraryDataSource: VideoLibraryDataSource,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoLibraryDataSource {
    override suspend fun getVideoLibrary(): List<VideoDomain> {
        return withContext(dispatcherIO) {
            val videoLibrary = offlineVideoLibraryDataSource.getVideoLibrary()
            val offlineVideo = videoDownloadDataSource.getDownloadedVideoList()

            videoLibrary.map { video ->
                offlineVideo.firstOrNull { it.id == video.id }
                    ?.copy(cacheState = VideoCacheState.Cached)
                    ?: run {
                        video
                    }
            }
        }
    }
}
