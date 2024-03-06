package com.jbm.module.core.network.video.library

import com.jbm.module.core.data.model.VideoDTO

interface VideoLibraryDataSource {
    suspend fun getVideoLibrary(): List<VideoDTO>
}
