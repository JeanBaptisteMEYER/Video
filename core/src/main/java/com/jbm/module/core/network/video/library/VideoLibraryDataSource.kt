package com.jbm.module.core.network.video.library

import com.jbm.module.core.model.VideoDomain

interface VideoLibraryDataSource {
    suspend fun getVideoLibrary(): List<VideoDomain>
}
