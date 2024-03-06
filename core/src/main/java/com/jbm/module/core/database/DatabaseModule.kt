package com.jbm.module.core.database

import android.content.Context
import androidx.room.Room
import com.jbm.module.core.database.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideMainDatabase(
        @ApplicationContext appContext: Context
    ): VideoDatabase {
        return Room.databaseBuilder(
            appContext,
            VideoDatabase::class.java,
            "VideoDatabase"
        ).build()
    }

    @Provides
    fun provideVideoDao(db: VideoDatabase): VideoDao {
        return db.videoDao()
    }
}
