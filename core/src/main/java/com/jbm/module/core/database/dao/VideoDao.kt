package com.jbm.module.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jbm.module.core.database.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM video")
    fun getAll(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM video WHERE id=:id")
    fun getById(id: String): VideoEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(videoEntity: VideoEntity)

    @Delete
    fun delete(videoEntity: VideoEntity)
}
