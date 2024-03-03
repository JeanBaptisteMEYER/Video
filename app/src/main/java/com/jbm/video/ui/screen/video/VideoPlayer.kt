package com.jbm.video.ui.screen.video

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import dagger.hilt.EntryPoints

/**
 * Composable function that displays a video player using ExoPlayer with Jetpack Compose.
 *
 * @param video The [VideoResultEntity] representing the video to be played.
 * @param playingIndex State that represents the current playing index.
 * @param onVideoChange Callback function invoked when the video changes.
 * @param isVideoEnded Callback function to determine whether the video has ended.
 * @param modifier Modifier for styling and positioning.
 *
 * @OptIn annotation to UnstableApi is used to indicate that the API is still experimental and may
 * undergo changes in the future.
 *
 * @SuppressLint annotation is used to suppress lint warning for the usage of OpaqueUnitKey.
 *
 * @ExperimentalAnimationApi annotation is used for the experimental Animation API usage.
 */
@OptIn(UnstableApi::class)
@SuppressLint("OpaqueUnitKey")
@ExperimentalAnimationApi
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    video: State<VideoDomain>,
    playingIndex: State<Int>,
    onVideoChange: (Int) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val cacheDataSourceFactory = EntryPoints.get(
        context.applicationContext,
        com.jbm.module.core.video.di.VideoPlayerEntryPoint::class.java
    ).getCacheDataSource()

    val isTitleVisible = remember { mutableStateOf(true) }
    val videoTitle = remember { mutableStateOf(video.value.name) }
    val isVideoCached =
        remember { mutableStateOf(video.value.cacheState == VideoCacheState.Cached) }

    // Create a list of MediaItems for the ExoPlayer
    val mediaItems = arrayListOf<MediaItem>()
    mediaItems.add(
        MediaItem.Builder()
            .setUri(video.value.videoUrl)
            .setMediaId(video.value.id)
            .setTag(video)
            .setMediaMetadata(MediaMetadata.Builder().setDisplayTitle(video.value.name).build())
            .build()
    )

    // Initialize ExoPlayer
    val exoPlayer = remember {
        if (isVideoCached.value) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
                )
                .build()
        } else {
            ExoPlayer.Builder(context).build()
        }.apply {
            setMediaItems(mediaItems)
            prepare()
            addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    // Hide video title after playing for 200 milliseconds
                    if (player.contentPosition >= 200) isTitleVisible.value = false
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    // Callback when the video changes
                    onVideoChange(this@apply.currentPeriodIndex)
                    isTitleVisible.value = true
                    videoTitle.value = mediaItem?.mediaMetadata?.displayTitle.toString()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    // Callback when the video playback state changes to STATE_ENDED
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        isVideoEnded.invoke(true)
                    }
                }
            })
        }
    }

    // Seek to the specified index and start playing
    exoPlayer.seekTo(playingIndex.value, C.TIME_UNSET)
    exoPlayer.playWhenReady = false

    // Add a lifecycle observer to manage player state based on lifecycle events
    LocalLifecycleOwner.current.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Start playing when the Composable is in the foreground
                    if (exoPlayer.isPlaying.not()) {
                        exoPlayer.play()
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    // Pause the player when the Composable is in the background
                    exoPlayer.pause()
                }

                else -> {
                    // Nothing
                }
            }
        }
    })

    // Column Composable to contain the video player
    Column(modifier = modifier.background(Color.Black)) {
        // DisposableEffect to release the ExoPlayer when the Composable is disposed
        DisposableEffect(
            AndroidView(
                modifier = modifier
                    .testTag("VIDEO_PLAYER_TAG"),
                factory = {
                    // AndroidView to embed a PlayerView into Compose
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                        setShowFastForwardButton(false)
                        setShowRewindButton(false)
                    }
                })
        ) {
            // Dispose the ExoPlayer when the Composable is disposed
            onDispose {
                exoPlayer.release()
            }
        }
    }
}
