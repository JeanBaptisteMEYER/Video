package com.jbm.video.ui.screen.video

import com.jbm.module.core.model.VideoDomain

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data class Success(val videoList: List<VideoDomain>) : VideoUiState
}
