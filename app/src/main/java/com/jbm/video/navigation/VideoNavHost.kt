package com.jbm.video.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.jbm.video.ui.screen.video.VIDEO_ROUTE
import com.jbm.video.ui.screen.video.videoScreen

@Composable
fun VideoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = VIDEO_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        videoScreen(
            onBackClick = navController::popBackStack
        )
    }
}
