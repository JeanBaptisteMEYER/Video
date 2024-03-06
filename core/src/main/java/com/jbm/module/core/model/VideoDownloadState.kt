package com.jbm.module.core.model

enum class VideoDownloadState(val value: Int) {
    Unknown(-1),
    Queued(0),
    Stopped(1),
    Downloading(2),
    Completed(3),
    Failed(4),
    Removing(5),
    Restarting(7);
}
