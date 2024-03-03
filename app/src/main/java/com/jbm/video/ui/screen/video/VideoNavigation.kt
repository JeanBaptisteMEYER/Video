package com.jbm.video.ui.screen.video

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val VIDEO_ROUTE = "video_route"

fun NavGraphBuilder.videoScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = VIDEO_ROUTE
    ) {
        VideoDestination(
            onBackClick = onBackClick
        )
    }
}
