package com.jbm.video.ui.screen.video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbm.module.core.data.repository.VideoRepository
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.takeWhile
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

    /**
     * Get the video from repository for given ID
     *
     * @param: ID of the video to be found
     */
    fun getVideoById(id: String) = viewModelScope.launch {
        videoRepository.getVideoById(id)
            .onSuccess { video ->
                //_uiState.update { VideoUiState.Success(video) }
            }
            .onFailure {
                Log.d("coucou", "Video $id Not Found: ")
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
