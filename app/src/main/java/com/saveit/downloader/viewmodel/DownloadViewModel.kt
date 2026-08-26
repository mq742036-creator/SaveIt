package com.saveit.downloader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.saveit.downloader.model.DownloadItem
import com.saveit.downloader.model.VideoInfo
import kotlin.random.Random

class DownloadViewModel : ViewModel() {
    private val _downloadItems = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadItems: StateFlow<List<DownloadItem>> = _downloadItems.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    fun analyzeUrl(url: String, callback: (VideoInfo?, String?) -> Unit) {
        viewModelScope.launch {
            // Simulate network delay for analysis
            delay(800 + Random.nextLong(200, 600))

            val platform = detectPlatform(url)
            if (platform == null) {
                callback(null, "Unsupported platform. Please use a supported URL.")
                return@launch
            }

            // Simulate fetching video info
            val qualities = when (Random.nextInt(1, 4)) {
                1 -> listOf(VideoInfo.Quality.P480, VideoInfo.Quality.P720)
                2 -> listOf(VideoInfo.Quality.P480, VideoInfo.Quality.P720, VideoInfo.Quality.P1080)
                else -> listOf(VideoInfo.Quality.P720, VideoInfo.Quality.P1080)
            }

            val info = VideoInfo(
                title = generateVideoTitle(platform),
                platform = platform,
                duration = formatDuration(Random.nextInt(30, 600)),
                fileSize = formatFileSize(Random.nextInt(5, 150) * 1024 * 1024L),
                thumbnail = null,
                qualities = qualities
            )

            callback(info, null)
        }
    }

    fun downloadVideo(
        url: String,
        quality: VideoInfo.Quality,
        onProgress: (Float) -> Unit,
        onComplete: (DownloadItem) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isDownloading.value) {
            onError("A download is already in progress")
            return
        }

        _isDownloading.value = true
        val platform = detectPlatform(url) ?: "Unknown"
        val qualityLabel = when (quality) {
            VideoInfo.Quality.P480 -> "480p"
            VideoInfo.Quality.P720 -> "720p"
            VideoInfo.Quality.P1080 -> "1080p"
        }

        val fileName = "${platform}_video_${System.currentTimeMillis()}.mp4"
        val downloadItem = DownloadItem(
            fileName = fileName,
            filePath = "/storage/emulated/0/Download/SaveIt/$fileName",
            fileSize = "Calculating...",
            url = url,
            platform = platform,
            quality = qualityLabel,
            status = DownloadItem.Status.DOWNLOADING,
            progress = 0f
        )

        // Add to list
        val currentItems = _downloadItems.value.toMutableList()
        currentItems.add(0, downloadItem)
        _downloadItems.value = currentItems

        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Simulate download with progress
                var progress = 0f
                while (progress < 1f) {
                    delay(100)
                    progress += 0.01f + Random.nextFloat() * 0.03f
                    if (progress > 1f) progress = 1f

                    val updatedItems = _downloadItems.value.toMutableList()
                    val index = updatedItems.indexOfFirst { it.id == downloadItem.id }
                    if (index != -1) {
                        updatedItems[index] = updatedItems[index].copy(
                            progress = progress,
                            status = DownloadItem.Status.DOWNLOADING
                        )
                        _downloadItems.value = updatedItems
                    }
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }

                // Simulate file size calculation
                val fileSizeBytes = Random.nextLong(10, 200) * 1024 * 1024

                // Mark as completed
                val completedItems = _downloadItems.value.toMutableList()
                val idx = completedItems.indexOfFirst { it.id == downloadItem.id }
                if (idx != -1) {
                    completedItems[idx] = completedItems[idx].copy(
                        status = DownloadItem.Status.COMPLETED,
                        progress = 1f,
                        fileSize = formatFileSize(fileSizeBytes)
                    )
                    _downloadItems.value = completedItems
                }

                withContext(Dispatchers.Main) {
                    onComplete(completedItems.find { it.id == downloadItem.id } ?: downloadItem)
                }
            } catch (e: Exception) {
                // Mark as failed
                val failedItems = _downloadItems.value.toMutableList()
                val idx = failedItems.indexOfFirst { it.id == downloadItem.id }
                if (idx != -1) {
                    failedItems[idx] = failedItems[idx].copy(
                        status = DownloadItem.Status.FAILED,
                        progress = 0f
                    )
                    _downloadItems.value = failedItems
                }
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Download failed")
                }
            } finally {
                _isDownloading.value = false
                downloadJobs.remove(downloadItem.id)
            }
        }

        downloadJobs[downloadItem.id] = job
    }

    fun deleteDownload(item: DownloadItem) {
        val items = _downloadItems.value.toMutableList()
        items.removeAll { it.id == item.id }
        _downloadItems.value = items

        // Cancel job if running
        downloadJobs[item.id]?.cancel()
        downloadJobs.remove(item.id)
    }

    fun retryDownload(item: DownloadItem) {
        deleteDownload(item)
        // Re-download with same parameters
        val quality = when (item.quality) {
            "480p" -> VideoInfo.Quality.P480
            "720p" -> VideoInfo.Quality.P720
            "1080p" -> VideoInfo.Quality.P1080
            else -> VideoInfo.Quality.P720
        }
        downloadVideo(
            url = item.url,
            quality = quality,
            onProgress = {},
            onComplete = {},
            onError = {}
        )
    }

    fun clearCompletedDownloads() {
        val items = _downloadItems.value.filter {
            it.status != DownloadItem.Status.COMPLETED
        }
        _downloadItems.value = items
    }

    private fun detectPlatform(url: String): String? {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
            url.contains("instagram.com") -> "Instagram"
            url.contains("tiktok.com") -> "TikTok"
            url.contains("twitter.com") || url.contains("x.com") -> "Twitter"
            url.contains("facebook.com") -> "Facebook"
            url.contains("reddit.com") -> "Reddit"
            url.contains("pinterest.com") -> "Pinterest"
            url.contains("vimeo.com") -> "Vimeo"
            else -> null
        }
    }

    private fun generateVideoTitle(platform: String): String {
        val titles = listOf(
            "Amazing ${platform} Video",
            "Best of ${platform}",
            "${platform} Highlights",
            "Trending on ${platform}",
            "${platform} Creator Content",
            "Viral ${platform} Video",
            "${platform} Shorts",
            "Premium ${platform} Content"
        )
        return titles[Random.nextInt(titles.size)]
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
