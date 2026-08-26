package com.saveit.downloader.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveit.downloader.R
import com.saveit.downloader.model.DownloadItem
import com.saveit.downloader.model.VideoInfo
import com.saveit.downloader.ui.theme.*
import com.saveit.downloader.viewmodel.DownloadViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: DownloadViewModel = viewModel(),
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var urlText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var videoInfo by remember { mutableStateOf<VideoInfo?>(null) }
    var selectedQuality by remember { mutableStateOf<VideoInfo.Quality?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var downloadedItem by remember { mutableStateOf<DownloadItem?>(null) }

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SaveItBackground,
                        SaveItSurface,
                        SaveItBackground
                    )
                )
            )
    ) {
        // Animated gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SaveItPrimary.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(
                            x = 200f + gradientOffset % 400f,
                            y = 300f + (gradientOffset * 0.7f) % 400f
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header with logo and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SaveItPrimary, SaveItSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SaveIt",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaveItText,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Social Media Downloader",
                            fontSize = 12.sp,
                            color = SaveItTextSecondary,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onNavigateToDownloads,
                        modifier = Modifier.size(40.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (viewModel.downloadItems.value.any { it.status == DownloadItem.Status.DOWNLOADING }) {
                                    Badge(
                                        containerColor = SaveItAccent,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Downloads",
                                tint = SaveItTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SaveItTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hero text
            Text(
                text = "Paste any video link",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SaveItText,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Download videos from YouTube, Instagram, TikTok, Twitter and more",
                fontSize = 14.sp,
                color = SaveItTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // URL Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SaveItSurfaceVariant.copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    1.dp,
                    SaveItPrimary.copy(alpha = if (isAnalyzing) 0.3f else 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Link",
                                tint = SaveItTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = urlText,
                                onValueChange = { 
                                    urlText = it
                                    videoInfo = null
                                    selectedQuality = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text(
                                        text = "Paste video URL here...",
                                        color = SaveItTextSecondary
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = SaveItText,
                                    fontSize = 14.sp
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        if (urlText.isNotBlank()) {
                                            analyzeUrl(urlText, viewModel) { info, error ->
                                                if (info != null) {
                                                    videoInfo = info
                                                    selectedQuality = info.qualities.firstOrNull()
                                                    isAnalyzing = false
                                                } else {
                                                    showError = true
                                                    errorMessage = error ?: "Failed to analyze URL"
                                                    isAnalyzing = false
                                                }
                                            }
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaveItPrimary.copy(alpha = 0.3f),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = SaveItPrimary,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                isError = showError,
                            )
                        }

                        // Paste button
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pastedText = clip.getItemAt(0).text.toString()
                                    if (pastedText.isNotBlank()) {
                                        urlText = pastedText
                                        videoInfo = null
                                        selectedQuality = null
                                        showError = false
                                        // Auto-analyze
                                        analyzeUrl(pastedText, viewModel) { info, error ->
                                            if (info != null) {
                                                videoInfo = info
                                                selectedQuality = info.qualities.firstOrNull()
                                                isAnalyzing = false
                                            } else {
                                                showError = true
                                                errorMessage = error ?: "Failed to analyze URL"
                                                isAnalyzing = false
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = SaveItPrimary
                            )
                        }

                        // Clear button
                        if (urlText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    urlText = ""
                                    videoInfo = null
                                    selectedQuality = null
                                    showError = false
                                    focusRequester.requestFocus()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SaveItTextSecondary
                                )
                            }
                        }
                    }

                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = SaveItError,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = SaveItError,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Analyze button
                    if (urlText.isNotBlank() && videoInfo == null && !isAnalyzing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                analyzeUrl(urlText, viewModel) { info, error ->
                                    if (info != null) {
                                        videoInfo = info
                                        selectedQuality = info.qualities.firstOrNull()
                                        isAnalyzing = false
                                    } else {
                                        showError = true
                                        errorMessage = error ?: "Failed to analyze URL"
                                        isAnalyzing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaveItPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Analyze",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze URL")
                        }
                    }

                    if (isAnalyzing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SaveItPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Analyzing video...",
                                color = SaveItTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Video Info Card (when analyzed)
            AnimatedVisibility(
                visible = videoInfo != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 })
            ) {
                videoInfo?.let { info ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SaveItSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            SaveItSecondary.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Platform badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (info.platform) {
                                                "YouTube" -> Color.Red.copy(alpha = 0.2f)
                                                "Instagram" -> Color(0xFFE1306C).copy(alpha = 0.2f)
                                                "TikTok" -> Color.Black.copy(alpha = 0.2f)
                                                "Twitter" -> Color(0xFF1DA1F2).copy(alpha = 0.2f)
                                                "Facebook" -> Color(0xFF1877F2).copy(alpha = 0.2f)
                                                else -> SaveItPrimary.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = info.platform,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when (info.platform) {
                                            "YouTube" -> Color.Red
                                            "Instagram" -> Color(0xFFE1306C)
                                            "TikTok" -> Color.White
                                            "Twitter" -> Color(0xFF1DA1F2)
                                            "Facebook" -> Color(0xFF1877F2)
                                            else -> SaveItText
                                        }
                                    )
                                }
                                Text(
                                    text = info.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaveItText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Duration and size
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Duration",
                                        tint = SaveItTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = info.duration,
                                        fontSize = 12.sp,
                                        color = SaveItTextSecondary
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SdStorage,
                                        contentDescription = "Size",
                                        tint = SaveItTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = info.fileSize,
                                        fontSize = 12.sp,
                                        color = SaveItTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quality selection
                            Text(
                                text = "Select Quality",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaveItTextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (info.qualities.size > 3) 140.dp else 80.dp)
                            ) {
                                items(info.qualities.size) { index ->
                                    val quality = info.qualities[index]
                                    QualityChip(
                                        quality = quality,
                                        isSelected = selectedQuality == quality,
                                        onClick = { selectedQuality = quality }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Download button
                            Button(
                                onClick = {
                                    if (selectedQuality != null && urlText.isNotBlank()) {
                                        isDownloading = true
                                        downloadProgress = 0f
                                        keyboardController?.hide()
                                        
                                        viewModel.downloadVideo(
                                            url = urlText,
                                            quality = selectedQuality!!,
                                            onProgress = { progress ->
                                                downloadProgress = progress
                                            },
                                            onComplete = { item ->
                                                isDownloading = false
                                                downloadProgress = 0f
                                                downloadedItem = item
                                                showSuccessDialog = true
                                                // Reset for next download
                                                videoInfo = null
                                                urlText = ""
                                                selectedQuality = null
                                            },
                                            onError = { error ->
                                                isDownloading = false
                                                downloadProgress = 0f
                                                showError = true
                                                errorMessage = error
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedQuality != null) SaveItPrimary else SaveItSurfaceVariant
                                ),
                                shape = RoundedCornerShape(14.dp),
                                enabled = selectedQuality != null && !isDownloading
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Downloading... ${(downloadProgress * 100).toInt()}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download Video",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            if (isDownloading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = downloadProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = SaveItSecondary,
                                    trackColor = SaveItSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Supported platforms
            Text(
                text = "Supported Platforms",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SaveItTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PlatformChip(name = "YouTube", color = Color.Red)
                PlatformChip(name = "Instagram", color = Color(0xFFE1306C))
                PlatformChip(name = "TikTok", color = Color(0xFF00F2EA))
                PlatformChip(name = "Twitter", color = Color(0xFF1DA1F2))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Success Dialog
        if (showSuccessDialog && downloadedItem != null) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    downloadedItem = null
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SaveItSuccess.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SaveItSuccess,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Download Complete!",
                        color = SaveItText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Your video has been downloaded successfully.",
                            color = SaveItTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        downloadedItem?.let {
                            Text(
                                text = "📁 ${it.fileName}",
                                color = SaveItText,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "📍 ${it.filePath}",
                                color = SaveItTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            downloadedItem = null
                        }
                    ) {
                        Text("Great!", color = SaveItPrimary)
                    }
                },
                containerColor = SaveItSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun QualityChip(
    quality: VideoInfo.Quality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (label, color) = when (quality) {
        VideoInfo.Quality.P480 -> "480p" to Quality480p
        VideoInfo.Quality.P720 -> "720p" to Quality720p
        VideoInfo.Quality.P1080 -> "1080p" to Quality1080p
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else SaveItTextSecondary
            )
        },
        modifier = Modifier
            .height(36.dp)
            .padding(0.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White,
            containerColor = SaveItSurfaceVariant.copy(alpha = 0.5f),
            labelColor = SaveItTextSecondary,
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun PlatformChip(
    name: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun analyzeUrl(
    url: String,
    viewModel: DownloadViewModel,
    callback: (VideoInfo?, String?) -> Unit
) {
    // Simulate analysis - in a real app, this would parse the URL
    // and fetch video info from the respective platform
    viewModel.analyzeUrl(url, callback)
}
