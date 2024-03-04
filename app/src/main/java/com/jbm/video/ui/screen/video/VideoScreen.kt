package com.jbm.video.ui.screen.video

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.video.R
import com.jbm.video.ui.screen.video.model.VideoUiState

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
            val playingVideoIndex = remember { mutableIntStateOf(0) }

            VideoScreen(
                videoPlaylist = data.videoList,
                playingVideoIndex = playingVideoIndex,
                onPlayingVideoIndexChange = {
                    playingVideoIndex.intValue = it
                },
                isVideoEnded = {},
                onVideoDownloadClick = viewModel::onVideoDownloadClick,
                onVideoDeleteClick = viewModel::onVideoDeleteClick
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
    playingVideoIndex: State<Int>,
    onPlayingVideoIndexChange: (Int) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
    onVideoDownloadClick: (VideoDomain) -> Unit,
    onVideoDeleteClick: (String) -> Unit
) {
    val videoPlaylistState = remember { mutableStateOf(videoPlaylist) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VideoPlayer(
            videoPlaylist = videoPlaylistState,
            playingVideoIndex = playingVideoIndex,
            onPlayingVideoIndexChange = onPlayingVideoIndexChange,
            isVideoEnded = isVideoEnded
        )
        VideoPlaylist(
            videoPlaylist = videoPlaylist,
            playingVideoIndex = playingVideoIndex,
            onPlayingVideoIndexChange = onPlayingVideoIndexChange,
            onVideoDownloadClick = onVideoDownloadClick,
            onVideoDeleteClick = onVideoDeleteClick
        )
    }
}

@Composable
fun VideoPlaylist(
    videoPlaylist: List<VideoDomain>,
    playingVideoIndex: State<Int>,
    onPlayingVideoIndexChange: (Int) -> Unit,
    onVideoDownloadClick: (VideoDomain) -> Unit,
    onVideoDeleteClick: (String) -> Unit
) {
    LazyColumn {
        itemsIndexed(videoPlaylist) { index, video ->
            VideoPlaylistItem(
                index = index,
                video = video,
                playingVideoIndex = playingVideoIndex,
                onPlayingVideoIndexChange = onPlayingVideoIndexChange,
                onVideoDownloadClick = onVideoDownloadClick,
                onVideoDeleteClick = onVideoDeleteClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlaylistItem(
    index: Int,
    video: VideoDomain,
    playingVideoIndex: State<Int>,
    onPlayingVideoIndexChange: (Int) -> Unit,
    onVideoDownloadClick: (VideoDomain) -> Unit,
    onVideoDeleteClick: (String) -> Unit
) {
    val currentlyPlaying = remember { mutableStateOf(false) }

    currentlyPlaying.value = index == playingVideoIndex.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (currentlyPlaying.value) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(end = 6.dp),
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    id = R.string.video_download_content_desc,
                ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            modifier = Modifier
                .basicMarquee()
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
                .weight(1f)
                .clickable {
                    onPlayingVideoIndexChange(index)
                },
            fontSize = 16.sp,
            fontWeight = if (currentlyPlaying.value) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            text = video.name
        )
        DownloadIcon(
            modifier = Modifier
                .fillMaxHeight(),
            video = video,
            onVideoDownloadClick = onVideoDownloadClick,
            onVideoDeleteClick = onVideoDeleteClick
        )
    }
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray
    )
}

@Composable
fun DownloadIcon(
    modifier: Modifier = Modifier,
    video: VideoDomain,
    onVideoDownloadClick: (VideoDomain) -> Unit,
    onVideoDeleteClick: (String) -> Unit
) {
    when (video.cacheState) {
        VideoCacheState.Cached -> {
            IconButton(
                modifier = Modifier.then(modifier),
                onClick = { onVideoDeleteClick(video.id) }
            ) {
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
            IconButton(
                modifier = Modifier.then(modifier),
                onClick = { onVideoDownloadClick(video) }
            ) {
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
            Box(
                modifier = Modifier
                    .then(modifier),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .wrapContentSize(Alignment.Center),
                    color = Color.Black,
                )
            }
        }
    }
}

@Preview
@Composable
fun VideoPlaylistPreview() {
    val playingVideoIndex = remember { mutableIntStateOf(0) }

    VideoPlaylist(
        videoPlaylist = listOf(
            VideoDomain(
                id = "1",
                name = "VideoName 1",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                cacheState = VideoCacheState.NotCached
            ),
            VideoDomain(
                id = "2",
                name = "VideoName 2",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                cacheState = VideoCacheState.NotCached
            ),
            VideoDomain(
                id = "3",
                name = "VideoName 3",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                cacheState = VideoCacheState.NotCached
            ),
            VideoDomain(
                id = "4",
                name = "VideoName 4",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                cacheState = VideoCacheState.NotCached
            ),
            VideoDomain(
                id = "5",
                name = "VideoName 5",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                cacheState = VideoCacheState.NotCached
            ),
        ),
        playingVideoIndex = playingVideoIndex,
        onPlayingVideoIndexChange = {},
        onVideoDownloadClick = {},
        onVideoDeleteClick = {}
    )
}
