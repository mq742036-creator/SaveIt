package com.saveit.downloader.model

import java.io.Serializable
import java.util.UUID

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val fileSize: String,
    val url: String,
    val platform: String,
    val quality: String,
    val status: Status = Status.PENDING,
    val progress: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    enum class Status {
        PENDING,
        DOWNLOADING,
        COMPLETED,
        FAILED
    }

    val statusText: String
        get() = when (status) {
            Status.PENDING -> "Pending"
            Status.DOWNLOADING -> "Downloading"
            Status.COMPLETED -> "Completed"
            Status.FAILED -> "Failed"
        }
}

data class VideoInfo(
    val title: String,
    val platform: String,
    val duration: String,
    val fileSize: String,
    val thumbnail: String? = null,
    val qualities: List<Quality>
) {
    enum class Quality {
        P480,
        P720,
        P1080
    }
}
