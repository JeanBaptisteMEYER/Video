package com.jbm.module.core.model

data class VideoDomain(
    val id: String,
    val name: String,
    val videoUrl: String,
    val cacheState: VideoCacheState
)

sealed interface VideoCacheState {
    data object Cached : VideoCacheState
    data object Caching : VideoCacheState
    data object NotCached : VideoCacheState
}
