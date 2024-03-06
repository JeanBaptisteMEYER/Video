package com.jbm.module.core.network.video.library

import android.content.res.AssetManager
import com.google.gson.Gson
import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.network.video.download.VideoDownloadDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class VideoDownloadModule {
    @OfflineVideoLibrary
    @Provides
    @Singleton
    fun provideOfflineVideoLibraryDataSource(
        gson: Gson,
        assets: AssetManager,
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): VideoLibraryDataSource {
        return OfflineVideoLibraryDataSourceImpl(
            gson,
            assets,
            dispatcherIO
        )
    }

    @Provides
    @Singleton
    fun provideVideoLibraryDataSource(
        videoDownloadDataSource: VideoDownloadDataSource,
        @OfflineVideoLibrary videoLibraryDataSource: VideoLibraryDataSource,
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): VideoLibraryDataSource {
        return VideoLibraryDataSourceImpl(
            videoDownloadDataSource,
            videoLibraryDataSource,
            dispatcherIO
        )
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class OfflineVideoLibrary
