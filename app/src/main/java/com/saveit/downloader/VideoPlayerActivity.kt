package com.saveit.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.saveit.downloader.ui.screens.VideoPlayerScreen
import com.saveit.downloader.ui.theme.SaveItTheme

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val filePath = intent.getStringExtra("file_path") ?: ""
        
        setContent {
            SaveItTheme {
                VideoPlayerScreen(
                    filePath = filePath,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
