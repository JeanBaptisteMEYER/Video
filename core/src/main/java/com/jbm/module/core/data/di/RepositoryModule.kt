package com.jbm.module.core.data.di

import android.content.Context
import android.content.res.AssetManager
import androidx.media3.exoplayer.offline.DownloadManager
import com.google.gson.Gson
import com.jbm.module.core.data.repository.VideoRepository
import com.jbm.module.core.data.repository.VideoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Provides
    fun provideVideoRepository(
        @ApplicationContext appContext: Context,
        gson: Gson,
        assets: AssetManager,
        downloadManager: DownloadManager,
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): VideoRepository = VideoRepositoryImpl(
        appContext,
        gson,
        assets,
        downloadManager,
        dispatcherIO
    )
}
