package com.jbm.module.core.model

sealed interface VideoDownloadState {
    data object Idle : VideoDownloadState
    data class Queued(val videoId: String) : VideoDownloadState
    data class Downloading(val videoId: String) : VideoDownloadState
    data class Stopped(val videoId: String) : VideoDownloadState
    data class Completed(val videoId: String) : VideoDownloadState
    data class Removing(val videoId: String) : VideoDownloadState
    data class Failed(val videoId: String) : VideoDownloadState
}
