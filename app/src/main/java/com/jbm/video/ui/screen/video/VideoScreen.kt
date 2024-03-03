package com.jbm.video.ui.screen.video

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoPlaybackState
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

            VideoScreen(
                videoPlaylist = data.videoList,
                playingIndex = data.videoList.find { it.playbackState == VideoPlaybackState.Playing }?.id,
                onPlayingVideoIdChange = viewModel::onPlayingVideoIdChange,
                isVideoEnded = {},
                onVideoItemClicked = viewModel::onPlayingVideoIdChange,
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
    videoPlaylist: List<VideoDomain>,
    playingIndex: String?,
    onPlayingVideoIdChange: (String) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
    onVideoItemClicked: (String) -> Unit,
    onVideoDownloadClick: (VideoDomain) -> Unit
) {
    val videoPlaylistState by remember { mutableStateOf(videoPlaylist) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VideoPlayer(
            videoPlaylist = videoPlaylistState,
            playingVideoId = playingIndex,
            onPlayingVideoIdChange = onPlayingVideoIdChange,
            isVideoEnded = isVideoEnded
        )
        VideoPlaylist(
            videoPlaylist = videoPlaylist,
            onVideoItemClicked = onVideoItemClicked,
            onVideoDownloadClick = onVideoDownloadClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlaylist(
    videoPlaylist: List<VideoDomain>,
    onVideoItemClicked: (String) -> Unit,
    onVideoDownloadClick: (VideoDomain) -> Unit
) {
    LazyColumn {
        items(videoPlaylist) { video ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .basicMarquee()
                        .clickable {
                            onVideoItemClicked(video.id)
                        },
                    fontWeight = if (video.playbackState == VideoPlaybackState.Playing) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    text = video.name
                )
                DownloadIcon(
                    video = video,
                    onVideoDownloadClick = onVideoDownloadClick
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun DownloadIcon(
    video: VideoDomain,
    onVideoDownloadClick: (VideoDomain) -> Unit
) {
    when (video.cacheState) {
        VideoCacheState.Cached -> {
            IconButton(onClick = { onVideoDownloadClick(video) }) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = stringResource(
                        id = R.string.video_download_content_desc,
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        VideoCacheState.NotCached -> {
            IconButton(onClick = { onVideoDownloadClick(video) }) {
                Icon(
                    imageVector = Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(
                        id = R.string.video_download_content_desc,
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        VideoCacheState.Caching -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(24.dp),
                color = Color.Black,
            )
        }
    }
}
