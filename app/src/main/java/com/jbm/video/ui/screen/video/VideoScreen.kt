package com.jbm.video.ui.screen.video

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.video.R

@Composable
fun VideoDestination(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getAllVideo()
    }

    when (val data = uiState.value) {
        is VideoUiState.Success -> {
            val playingIndex = rememberSaveable { mutableIntStateOf(1) }

            VideoScreen(
                videoState = data,
                playingIndex = playingIndex,
                onVideoChange = {},
                isVideoEnded = {},
                onVideoDownloadClick = viewModel::downloadVideo
            )
        }

        is VideoUiState.Loading -> {
            Text(text = "Loading...")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoScreen(
    videoState: VideoUiState.Success,
    playingIndex: State<Int>,
    onVideoChange: (Int) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
    onVideoDownloadClick: (com.jbm.module.core.model.VideoDomain) -> Unit
) {
    val currentVideo = remember { mutableStateOf(videoState.videoList.random()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VideoPlayer(
            video = currentVideo,
            playingIndex = playingIndex,
            onVideoChange = onVideoChange,
            isVideoEnded = isVideoEnded
        )

        LazyColumn {
            items(videoState.videoList) { video ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = video.name)
                    Button(onClick = { onVideoDownloadClick(video) }) {
                        when (video.cacheState) {
                            com.jbm.module.core.model.VideoCacheState.Cached -> {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = stringResource(
                                        id = R.string.video_download_content_desc,
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            com.jbm.module.core.model.VideoCacheState.NotCached -> {
                                Icon(
                                    imageVector = Icons.Rounded.FavoriteBorder,
                                    contentDescription = stringResource(
                                        id = R.string.video_download_content_desc,
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            com.jbm.module.core.model.VideoCacheState.Caching -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .align(alignment = Alignment.CenterVertically),
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
