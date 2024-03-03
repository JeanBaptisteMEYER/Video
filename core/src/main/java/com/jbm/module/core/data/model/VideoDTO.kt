package com.jbm.module.core.data.model

import com.google.gson.annotations.SerializedName

data class VideoListDTO(
    @SerializedName("videos") val videoList: List<VideoDTO>
)

data class VideoDTO(
    @SerializedName("description") val description: String,
    @SerializedName("sources") val videoUrl: List<String>,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("thumb") val thumb: String,
    @SerializedName("title") val title: String,
)
