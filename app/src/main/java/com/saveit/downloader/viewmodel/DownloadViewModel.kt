package com.saveit.downloader.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveit.downloader.model.DownloadItem
import com.saveit.downloader.model.VideoInfo
// ✅ CORRECT PACKAGE for the library
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class DownloadViewModel : ViewModel() {
    private val _downloadItems = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadItems: StateFlow<List<DownloadItem>> = _downloadItems.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    fun analyzeUrl(url: String, callback: (VideoInfo?, String?) -> Unit) {
        viewModelScope.launch {
            delay(800 + Random.nextLong(200, 600))

            val platform = detectPlatform(url)
            if (platform == null) {
                callback(null, "Unsupported platform. Please use a supported URL.")
                return@launch
            }

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

    fun downloadVideoReal(
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

        val formatOption = when (quality) {
            VideoInfo.Quality.P480 -> "best[height<=480]"
            VideoInfo.Quality.P720 -> "best[height<=720]"
            VideoInfo.Quality.P1080 -> "best[height<=1080]"
        }

        val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SaveIt")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        // Create download item
        val downloadItem = DownloadItem(
            fileName = "Downloading...",
            filePath = "",
            fileSize = "Calculating...",
            url = url,
            platform = platform,
            quality = qualityLabel,
            status = DownloadItem.Status.DOWNLOADING,
            progress = 0f
        )

        val currentItems = _downloadItems.value.toMutableList()
        currentItems.add(0, downloadItem)
        _downloadItems.value = currentItems
        val itemId = downloadItem.id

        // Build request
        val outputTemplate = File(downloadDir, "%(title)s_%(height)sp.%(ext)s").absolutePath
        val request = YoutubeDLRequest(url)
        request.option("--no-playlist")
        request.option("-f", formatOption)
        request.option("-o", outputTemplate)

        viewModelScope.launch {
            try {
                val resultFile = suspendCancellableCoroutine<File> { continuation ->
                    // ✅ FIXED: Explicit parameter types for lambda
                    YoutubeDL.getInstance().execute(request) { progress: Int, eta: Long, line: String ->
                        val progressPercent = progress.toFloat() / 100f
                        viewModelScope.launch(Dispatchers.Main) {
                            onProgress(progressPercent)
                            val items = _downloadItems.value.toMutableList()
                            val index = items.indexOfFirst { it.id == itemId }
                            if (index != -1) {
                                items[index] = items[index].copy(
                                    progress = progressPercent,
                                    status = DownloadItem.Status.DOWNLOADING
                                )
                                _downloadItems.value = items
                            }
                        }
                    }?.let { command ->
                        // This runs after completion
                        val files = downloadDir.listFiles()
                        val latest = files?.maxByOrNull { it.lastModified() }
                        if (latest != null) {
                            continuation.resume(latest)
                        } else {
                            continuation.resumeWithException(Exception("No output file found"))
                        }
                    }
                }

                _isDownloading.value = false

                val finalItem = DownloadItem(
                    id = itemId,
                    fileName = resultFile.name,
                    filePath = resultFile.absolutePath,
                    fileSize = formatFileSize(resultFile.length()),
                    url = url,
                    platform = platform,
                    quality = qualityLabel,
                    status = DownloadItem.Status.COMPLETED,
                    progress = 1f,
                    timestamp = System.currentTimeMillis()
                )

                val items = _downloadItems.value.toMutableList()
                val index = items.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    items[index] = finalItem
                    _downloadItems.value = items
                }

                Log.d("SaveIt", "✅ Download complete: ${resultFile.absolutePath}")
                onComplete(finalItem)

            } catch (e: Exception) {
                _isDownloading.value = false
                Log.e("SaveIt", "❌ Download failed", e)

                val items = _downloadItems.value.toMutableList()
                val index = items.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    items[index] = items[index].copy(
                        status = DownloadItem.Status.FAILED,
                        progress = 0f
                    )
                    _downloadItems.value = items
                }

                onError(e.message ?: "Download failed")
            }
        }
    }

    fun downloadVideo(
        url: String,
        quality: VideoInfo.Quality,
        onProgress: (Float) -> Unit,
        onComplete: (DownloadItem) -> Unit,
        onError: (String) -> Unit
    ) {
        downloadVideoReal(url, quality, onProgress, onComplete, onError)
    }

    fun deleteDownload(item: DownloadItem) {
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) { /* ignore */ }

        val items = _downloadItems.value.toMutableList()
        items.removeAll { it.id == item.id }
        _downloadItems.value = items
        downloadJobs[item.id]?.cancel()
        downloadJobs.remove(item.id)
    }

    fun retryDownload(item: DownloadItem) {
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) { /* ignore */ }

        deleteDownload(item)
        val quality = when (item.quality) {
            "480p" -> VideoInfo.Quality.P480
            "720p" -> VideoInfo.Quality.P720
            "1080p" -> VideoInfo.Quality.P1080
            else -> VideoInfo.Quality.P720
        }
        downloadVideoReal(item.url, quality, {}, {}, {})
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
