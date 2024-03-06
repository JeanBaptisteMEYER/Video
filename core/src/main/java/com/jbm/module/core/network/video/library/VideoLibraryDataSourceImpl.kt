package com.jbm.module.core.network.video.library

import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.data.model.VideoDTO
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class VideoLibraryDataSourceImpl @Inject constructor(
    private val videoDownloadDataSource: VideoDownloadDataSource,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoLibraryDataSource {
    override suspend fun getVideoLibrary(): List<VideoDTO> {
        TODO("not yet implemented")
    }
}
