package com.jbm.module.core.data.di

import com.jbm.module.core.data.repository.VideoRepository
import com.jbm.module.core.data.repository.VideoRepositoryImpl
import com.jbm.module.core.database.dao.VideoDao
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import com.jbm.module.core.network.video.library.FakeVideoLibrary
import com.jbm.module.core.network.video.library.VideoLibraryDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Provides
    fun provideVideoRepository(
        videoDao: VideoDao,
        @FakeVideoLibrary videoLibraryDataSource: VideoLibraryDataSource,
        videoDownloadDataSource: VideoDownloadDataSource,
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): VideoRepository = VideoRepositoryImpl(
        videoDao,
        videoLibraryDataSource,
        videoDownloadDataSource,
        dispatcherIO
    )
}
