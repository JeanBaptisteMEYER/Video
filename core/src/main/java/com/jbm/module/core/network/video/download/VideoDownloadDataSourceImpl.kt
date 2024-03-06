package com.jbm.module.core.network.video.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import com.jbm.module.core.model.VideoDownloadState
import com.jbm.module.core.network.video.download.service.VideoDownloadService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class VideoDownloadDataSourceImpl @Inject constructor(
    private val appContext: Context,
    private val downloadManager: DownloadManager,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoDownloadDataSource {

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
                val downloadListener = object : DownloadManager.Listener {
                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?
                    ) {
                        super.onDownloadChanged(downloadManager, download, finalException)
                        when (download.state) {
                            Download.STATE_COMPLETED -> {
                                trySend(VideoDownloadState.Completed(download.request.id))
                            }

                            Download.STATE_DOWNLOADING -> {
                                trySend(VideoDownloadState.Downloading(download.request.id))
                            }

                            Download.STATE_FAILED -> {
                                trySend(VideoDownloadState.Failed(download.request.id))
                            }

                            Download.STATE_QUEUED -> {
                                trySend(VideoDownloadState.Queued(download.request.id))
                            }

                            Download.STATE_REMOVING -> {
                                trySend(VideoDownloadState.Removing(download.request.id))
                            }

                            Download.STATE_RESTARTING -> {
                                trySend(VideoDownloadState.Downloading(download.request.id))
                            }

                            Download.STATE_STOPPED -> {
                                trySend(VideoDownloadState.Stopped(download.request.id))
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
                val downloadListener = object : DownloadManager.Listener {
                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?
                    ) {
                        super.onDownloadChanged(downloadManager, download, finalException)
                        when (download.state) {
                            Download.STATE_COMPLETED -> {
                                trySend(VideoDownloadState.Completed(download.request.id))
                            }

                            Download.STATE_DOWNLOADING -> {
                                trySend(VideoDownloadState.Downloading(download.request.id))
                            }

                            Download.STATE_FAILED -> {
                                trySend(VideoDownloadState.Failed(download.request.id))
                            }

                            Download.STATE_QUEUED -> {
                                trySend(VideoDownloadState.Queued(download.request.id))
                            }

                            Download.STATE_REMOVING -> {
                                trySend(VideoDownloadState.Removing(download.request.id))
                            }

                            Download.STATE_RESTARTING -> {
                                trySend(VideoDownloadState.Downloading(download.request.id))
                            }

                            Download.STATE_STOPPED -> {
                                trySend(VideoDownloadState.Stopped(download.request.id))
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
    override suspend fun getDownloadedVideoList(): List<VideoDomain> {
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
                        cacheState = VideoCacheState.Cached
                    )
                )
            } while (downloadCursor.moveToNext())
        }
        return downloadedTracks.toList()
    }
}
