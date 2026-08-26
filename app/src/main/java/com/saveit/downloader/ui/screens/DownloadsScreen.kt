package com.saveit.downloader.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveit.downloader.model.DownloadItem
import com.saveit.downloader.ui.theme.*
import com.saveit.downloader.viewmodel.DownloadViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val downloadItems by viewModel.downloadItems.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaveItBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SaveItText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "My Downloads",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SaveItText
            )
            Spacer(modifier = Modifier.weight(1f))
            if (downloadItems.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearCompletedDownloads() },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = SaveItError
                    )
                ) {
                    Text("Clear All", fontSize = 13.sp)
                }
            }
        }

        if (downloadItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        SaveItPrimary.copy(alpha = 0.2f),
                                        SaveItSecondary.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Empty",
                            tint = SaveItTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        text = "No Downloads Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaveItText
                    )
                    Text(
                        text = "Downloaded videos will appear here",
                        fontSize = 14.sp,
                        color = SaveItTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloadItems) { item ->
                    DownloadItemCard(
                        item = item,
                        onDelete = { viewModel.deleteDownload(item) },
                        onRetry = { viewModel.retryDownload(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = SaveItSurfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (item.status) {
                DownloadItem.Status.COMPLETED -> SaveItSuccess.copy(alpha = 0.2f)
                DownloadItem.Status.DOWNLOADING -> SaveItSecondary.copy(alpha = 0.2f)
                DownloadItem.Status.FAILED -> SaveItError.copy(alpha = 0.2f)
                DownloadItem.Status.PENDING -> SaveItWarning.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (item.status) {
                            DownloadItem.Status.COMPLETED -> SaveItSuccess.copy(alpha = 0.15f)
                            DownloadItem.Status.DOWNLOADING -> SaveItSecondary.copy(alpha = 0.15f)
                            DownloadItem.Status.FAILED -> SaveItError.copy(alpha = 0.15f)
                            DownloadItem.Status.PENDING -> SaveItWarning.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item.status) {
                    DownloadItem.Status.COMPLETED -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = SaveItSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    DownloadItem.Status.DOWNLOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SaveItSecondary,
                        strokeWidth = 2.dp
                    )
                    DownloadItem.Status.FAILED -> Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = SaveItError,
                        modifier = Modifier.size(22.dp)
                    )
                    DownloadItem.Status.PENDING -> Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Pending",
                        tint = SaveItWarning,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SaveItText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.fileSize,
                        fontSize = 12.sp,
                        color = SaveItTextSecondary
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = SaveItTextSecondary
                    )
                    Text(
                        text = item.statusText,
                        fontSize = 12.sp,
                        color = when (item.status) {
                            DownloadItem.Status.COMPLETED -> SaveItSuccess
                            DownloadItem.Status.DOWNLOADING -> SaveItSecondary
                            DownloadItem.Status.FAILED -> SaveItError
                            DownloadItem.Status.PENDING -> SaveItWarning
                        }
                    )
                    if (item.status == DownloadItem.Status.DOWNLOADING && item.progress > 0) {
                        Text(
                            text = "• ${(item.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = SaveItTextSecondary
                        )
                    }
                }
            }

            when (item.status) {
                DownloadItem.Status.COMPLETED -> {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SaveItTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DownloadItem.Status.FAILED -> {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = SaveItPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SaveItTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DownloadItem.Status.DOWNLOADING -> {
                    IconButton(
                        onClick = { /* Cancel download */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = SaveItError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                else -> {}
            }
        }

        if (item.status == DownloadItem.Status.DOWNLOADING) {
            LinearProgressIndicator(
                progress = item.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SaveItSecondary,
                trackColor = SaveItSurfaceVariant
            )
        }
    }
}
