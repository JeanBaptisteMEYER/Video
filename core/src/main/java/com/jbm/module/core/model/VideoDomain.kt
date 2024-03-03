package com.jbm.module.core.model

const val EXAMPLE_VIDEO_URI =
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

data class VideoDomain(
    val id: String,
    val name: String,
    val videoUrl: String,
    val playbackState: VideoPlaybackState,
    val cacheState: VideoCacheState
)

sealed interface VideoPlaybackState {
    data object Playing : VideoPlaybackState
    data object Idle : VideoPlaybackState
}

sealed interface VideoCacheState {
    data object Cached : VideoCacheState
    data object Caching : VideoCacheState
    data object NotCached : VideoCacheState
}
