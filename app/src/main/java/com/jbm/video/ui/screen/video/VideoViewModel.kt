package com.jbm.video.ui.screen.video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbm.module.core.data.repository.VideoRepository
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import com.jbm.module.core.model.VideoPlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Loading)
    val uiState = _uiState.asStateFlow()

    /**
     * Get all videos from repository
     */
    fun getAllVideo() = viewModelScope.launch {
        videoRepository.getAllVideos()
            .onSuccess { videoList ->
                updateUiState(VideoUiState.Success(videoList))
            }
            .onFailure {
                Log.d("coucou", "VideoList Not Found: ")
            }
    }

    fun onPlayingVideoIdChange(videoId: String) {
        (uiState.value as? VideoUiState.Success)?.videoList?.map {
            if (it.id == videoId) {
                it.copy(playbackState = VideoPlaybackState.Playing)
            } else {
                it.copy(playbackState = VideoPlaybackState.Idle)
            }
        }?.let {
            updateUiState(VideoUiState.Success(it))
        }
    }

    /**
     * Download a given Video. The Video will be store in app files and made accessible offline
     *
     * @param: VideoDomain to be download
     */
    fun downloadVideo(video: VideoDomain) {
        viewModelScope.launch {
            if (video.cacheState is VideoCacheState.Cached) {
                videoRepository.deleteDownloadedVideoById(video.id)
                    .cancellable()
                    .collect { videoDownloadState ->
                        when(videoDownloadState) {
                            is VideoDownloadState.Removing -> {
                                (uiState.value as? VideoUiState.Success)?.videoList?.map {
                                    if (it.id == videoDownloadState.videoId) it.copy(cacheState = VideoCacheState.NotCached) else it
                                }?.let {
                                    updateUiState(VideoUiState.Success(it))
                                }
                                coroutineContext.job.cancel()
                            }
                            else -> {}
                        }
                    }
            } else {
                videoRepository.downloadVideo(video = video)
                    .cancellable()
                    .collect { videoDownloadState ->
                        when (videoDownloadState) {
                            is VideoDownloadState.Downloading -> {
                                (uiState.value as? VideoUiState.Success)?.videoList?.map {
                                    if (it.id == videoDownloadState.videoId) {
                                        it.copy(cacheState = VideoCacheState.Caching)
                                    } else {
                                        it
                                    }
                                }?.let {
                                    updateUiState(VideoUiState.Success(it))
                                }
                            }

                            is VideoDownloadState.Completed -> {
                                (uiState.value as? VideoUiState.Success)?.videoList?.map {
                                    if (it.id == videoDownloadState.videoId) it.copy(cacheState = VideoCacheState.Cached) else it
                                }?.let {
                                    updateUiState(VideoUiState.Success(it))
                                }
                                coroutineContext.job.cancel()
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun updateUiState(state: VideoUiState) {
        _uiState.update { state }
    }
}
