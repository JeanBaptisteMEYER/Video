package com.jbm.module.core.network.video.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource.Factory
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import com.jbm.module.core.data.di.DispatcherIO
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
fun interface VideoDownloadServiceEntryPoint {
    fun getDownloadManager(): DownloadManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
fun interface VideoPlayerEntryPoint {
    fun getCacheDataSource(): DataSource.Factory
}

@InstallIn(SingletonComponent::class)
@Module
class VideoDownloadModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoDatabase(@ApplicationContext appContext: Context): DatabaseProvider {
        return StandaloneDatabaseProvider(appContext)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoCache(
        @ApplicationContext appContext: Context,
        dataBase: DatabaseProvider
    ): Cache {
        val downloadContentDirectory = File(appContext.filesDir, "phrase_app")
        return SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), dataBase)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoDataSource(): Factory {
        return Factory()
    }

    @Provides
    @Singleton
    fun provideExecutor(): Executor {
        return Executor(Runnable::run)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext appContext: Context,
        dataBase: DatabaseProvider,
        downloadCache: Cache,
        dataSource: Factory,
        downloadExecutor: Executor
    ): DownloadManager {
        return DownloadManager(
            appContext,
            dataBase,
            downloadCache,
            dataSource,
            downloadExecutor
        )
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(
        downloadCache: Cache,
        dataSource: Factory,
    ): DataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(dataSource)
            .setCacheWriteDataSinkFactory(null) // Disable writing.
    }

    @Provides
    @Singleton
    fun provideVideoDownloadDataSource(
        @ApplicationContext appContext: Context,
        downloadManager: DownloadManager,
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): VideoDownloadDataSource {
        return VideoDownloadDataSourceImpl(
            appContext,
            downloadManager,
            dispatcherIO
        )
    }
}
