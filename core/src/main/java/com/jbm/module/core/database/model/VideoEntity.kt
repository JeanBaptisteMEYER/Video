package com.jbm.module.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import kotlinx.datetime.Instant

@Entity(tableName = "video")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 1,
    val title: String,
    val subTitle: String,
    val description: String,
    val videoUrl: String,
    val thumb: String,
    val createdAt: Instant
)

fun VideoEntity.toDomain(): VideoDomain =
    VideoDomain(
        this.id.toString(),
        this.title,
        this.videoUrl,
        VideoCacheState.NotCached
    )
