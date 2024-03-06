package com.jbm.module.core.network.video.library

import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.jbm.module.core.data.di.DispatcherIO
import com.jbm.module.core.data.model.VideoDTO
import com.jbm.module.core.data.model.VideoListDTO
import com.jbm.module.core.model.VideoCacheState
import com.jbm.module.core.model.VideoDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class OfflineVideoLibraryDataSourceImpl @Inject constructor(
    private val gson: Gson,
    private val assets: AssetManager,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : VideoLibraryDataSource {

    override suspend fun getVideoLibrary(): List<VideoDomain> {
        return withContext(dispatcherIO) {
            // Read File
            val jsonString: String
            try {
                jsonString = assets.open("public_video_list.json")
                    .bufferedReader()
                    .use { it.readText() }
            } catch (ioException: IOException) {
                return@withContext listOf()
            }

            // Parse File
            val videoListType = object : TypeToken<VideoListDTO>() {}.type
            val videoList: List<VideoDTO>
            try {
                videoList = gson.fromJson<VideoListDTO>(jsonString, videoListType).videoList
            } catch (jsonSyntaxException: JsonSyntaxException) {
                return@withContext listOf()
            }

            return@withContext videoList.mapIndexed { index, video ->
                VideoDomain(
                    id = (index + 1).toString(),
                    name = video.title,
                    videoUrl = video.videoUrl.first(),
                    cacheState = VideoCacheState.NotCached
                )
            }
        }
    }
}
