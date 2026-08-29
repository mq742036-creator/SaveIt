package com.saveit.downloader.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.saveit.downloader.ui.theme.SaveItBackground
import com.saveit.downloader.ui.theme.SaveItPrimary
import com.saveit.downloader.ui.theme.SaveItText
import java.io.File

@Composable
fun VideoPlayerScreen(
    filePath: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Create ExoPlayer instance
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(File(filePath)))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    // Clean up player when screen is closed
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    
    Scaffold(
        containerColor = SaveItBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Playing Video",
                        color = SaveItText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SaveItText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaveItBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SaveItBackground)
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = true
                        keepScreenOn = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
