package com.jbm.module.core.data.repository

import android.content.Context
import android.content.res.AssetManager
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadManager.Listener
import androidx.media3.exoplayer.offline.DownloadService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.data.model.VideoDTO
import com.jbm.module.core.data.model.VideoListDTO
import com.jbm.module.core.model.EXAMPLE_VIDEO_URI
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import com.jbm.module.core.model.VideoPlaybackState
import com.jbm.module.core.video.VideoDownloadService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val appContext: Context,
    private val gson: Gson,
    private val assets: AssetManager,
    private val downloadManager: DownloadManager,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoRepository {
    override suspend fun getAllVideos(): Result<List<VideoDomain>> {
        return withContext(dispatcherIO) {
            // Read File
            val jsonString: String
            try {
                jsonString = assets.open("public_video_list.json")
                    .bufferedReader()
                    .use { it.readText() }
            } catch (ioException: IOException) {
                return@withContext Result.failure(ioException)
            }

            // Parse File
            val videoListType = object : TypeToken<VideoListDTO>() {}.type
            val videoList: List<VideoDTO>
            try {
                videoList = gson.fromJson<VideoListDTO>(jsonString, videoListType).videoList
            } catch (jsonSyntaxException: JsonSyntaxException) {
                return@withContext Result.failure(jsonSyntaxException)
            }


            val videoLibrary = videoList.mapIndexed { index, video ->
                VideoDomain(
                    id = (index + 1).toString(),
                    name = video.title,
                    videoUrl = video.videoUrl.first(),
                    playbackState = if (index == 0) VideoPlaybackState.Playing else VideoPlaybackState.Idle,
                    cacheState = VideoCacheState.NotCached
                )
            }

            return@withContext Result.success(
                videoLibrary.map { video ->
                    getDownloadedVideoList().getOrNull()?.let { cachedVideoList ->
                        if (cachedVideoList.firstOrNull { it.id == video.id } != null) {
                            return@map video.copy(cacheState = VideoCacheState.Cached)
                        }
                    }
                    return@map video
                }
            )
        }
    }

    override suspend fun getVideoById(id: String): Result<VideoDomain> {
        return Result.success(
            VideoDomain(
                id = "1",
                name = "Video Name",
                videoUrl = EXAMPLE_VIDEO_URI,
                playbackState = VideoPlaybackState.Idle,
                cacheState = VideoCacheState.NotCached
            )
        )
    }

    /**
     * Download a given video. The video will then be accessible offline
     *
     * @param video The [VideoDomain] to be download
     * @return [Flow] of [VideoDownloadState] representing the state of the download
     */
    @OptIn(UnstableApi::class)
    override suspend fun downloadVideo(video: VideoDomain): Flow<VideoDownloadState> {
        return withContext(dispatcherIO) {
            val mediaItem = MediaItem.Builder()
                .setUri(video.videoUrl)
                .setMediaId(video.id)
                .setTag(video)
                .setMediaMetadata(MediaMetadata.Builder().setDisplayTitle(video.name).build())
                .build()

            val helper = DownloadHelper.forMediaItem(appContext, mediaItem)
            helper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(helper: DownloadHelper) {
                    val json = JSONObject()
                    //extra data about the download like title, artist e.tc below is an example
                    json.put("id", mediaItem.mediaId)
                    json.put("title", mediaItem.mediaMetadata.displayTitle)
                    val download =
                        helper.getDownloadRequest(
                            mediaItem.mediaId,
                            Util.getUtf8Bytes(json.toString())
                        )

                    //sending the request to the download service
                    DownloadService.sendAddDownload(
                        appContext,
                        VideoDownloadService::class.java,
                        download,
                        false
                    )
                }

                override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                    e.printStackTrace()
                }
            })

            return@withContext callbackFlow {
                val downloadListener = object : Listener {
                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?
                    ) {
                        super.onDownloadChanged(downloadManager, download, finalException)
                        when (download.state) {

                            Download.STATE_COMPLETED -> {
                                trySend(VideoDownloadState.Completed(video.id))
                            }

                            Download.STATE_DOWNLOADING -> {
                                trySend(VideoDownloadState.Downloading(video.id))
                            }

                            Download.STATE_FAILED -> {
                                trySend(VideoDownloadState.Failed(video.id))
                            }

                            Download.STATE_QUEUED -> {
                                trySend(VideoDownloadState.Queued(video.id))
                            }

                            Download.STATE_REMOVING -> {
                                trySend(VideoDownloadState.Removing(video.id))
                            }

                            Download.STATE_RESTARTING -> {
                                trySend(VideoDownloadState.Downloading(video.id))
                            }

                            Download.STATE_STOPPED -> {
                                trySend(VideoDownloadState.Stopped(video.id))
                            }
                        }
                    }
                }

                downloadManager.addListener(downloadListener)
                awaitClose { downloadManager.removeListener(downloadListener) }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun deleteDownloadedVideoById(videoId: String): Flow<VideoDownloadState> {
        return withContext(dispatcherIO) {
            DownloadService.sendRemoveDownload(
                appContext,
                VideoDownloadService::class.java,
                videoId,
                false
            )

            return@withContext callbackFlow {
                val downloadListener = object : Listener {
                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?
                    ) {
                        super.onDownloadChanged(downloadManager, download, finalException)
                        when (download.state) {

                            Download.STATE_COMPLETED -> {
                                trySend(VideoDownloadState.Completed(videoId))
                            }

                            Download.STATE_DOWNLOADING -> {
                                trySend(VideoDownloadState.Downloading(videoId))
                            }

                            Download.STATE_FAILED -> {
                                trySend(VideoDownloadState.Failed(videoId))
                            }

                            Download.STATE_QUEUED -> {
                                trySend(VideoDownloadState.Queued(videoId))
                            }

                            Download.STATE_REMOVING -> {
                                trySend(VideoDownloadState.Removing(videoId))
                            }

                            Download.STATE_RESTARTING -> {
                                trySend(VideoDownloadState.Downloading(videoId))
                            }

                            Download.STATE_STOPPED -> {
                                trySend(VideoDownloadState.Stopped(videoId))
                            }
                        }
                    }
                }

                downloadManager.addListener(downloadListener)
                awaitClose { downloadManager.removeListener(downloadListener) }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun getDownloadedVideoList(): Result<List<VideoDomain>> {
        val downloadedTracks = ArrayList<VideoDomain>()
        val downloadCursor: DownloadCursor = downloadManager.downloadIndex.getDownloads()
        if (downloadCursor.moveToFirst()) {
            do {
                val jsonString = Util.fromUtf8Bytes(downloadCursor.download.request.data)
                val jsonObject = JSONObject(jsonString)
                val uri = downloadCursor.download.request.uri

                downloadedTracks.add(
                    VideoDomain(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("title"),
                        videoUrl = uri.toString(),
                        playbackState = VideoPlaybackState.Idle,
                        cacheState = VideoCacheState.Cached
                    )
                )
            } while (downloadCursor.moveToNext())
        }
        return Result.success(downloadedTracks.toList())
    }
}
