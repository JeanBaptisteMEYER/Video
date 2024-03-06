package com.jbm.module.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jbm.module.core.database.dao.VideoDao
import com.jbm.module.core.database.model.VideoEntity
import com.jbm.module.core.database.utils.Converters

@Database(
    version = 1,
    entities = [VideoEntity::class],
    exportSchema = true,
)
@TypeConverters(Converters::class)
internal abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
