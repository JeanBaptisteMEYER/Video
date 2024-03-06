package com.jbm.video.ui.screen.video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbm.module.core.data.repository.VideoRepository
import com.jbm.module.core.model.VideoDomain
import com.jbm.video.ui.screen.video.model.VideoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        videoRepository.getVideoLibraryAsFlow()
            .collect { videoList ->
                Log.d("coucou", "NewVideoList: $videoList")
                updateUiState(VideoUiState.Success(videoList))
            }
    }

    /**
     * Download a given Video. The Video will be store in app files and made accessible offline
     *
     * @param: VideoDomain to be download
     */
    fun onVideoDownloadClick(video: VideoDomain) {
        viewModelScope.launch {
            videoRepository.downloadVideo(video = video)
        }
    }

    fun onVideoDeleteClick(videoId: VideoDomain) {
        viewModelScope.launch {
            videoRepository.deleteDownloadedVideoById(videoId)
        }
    }

    private fun updateUiState(state: VideoUiState) {
        _uiState.update { state }
    }
}
