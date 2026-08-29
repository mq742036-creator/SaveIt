package com.saveit.downloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saveit.downloader.ui.screens.DownloadsScreen
import com.saveit.downloader.ui.screens.HomeScreen
import com.saveit.downloader.ui.screens.SettingsScreen
import com.saveit.downloader.ui.screens.VideoPlayerScreen
import com.saveit.downloader.ui.theme.SaveItTheme
import com.saveit.downloader.viewmodel.DownloadViewModel
// ✅ NEW IMPORTS for youtubedl-android library
import com.github.yausername.youtubedl_android.YoutubeDL
import com.github.yausername.youtubedl_android.YoutubeDLRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🎯 Initialize youtube-dl engine
        try {
            // Check if the YouTubeDL instance is already initialized
            if (!YoutubeDL.getInstance().isInitialized) {
                YoutubeDL.getInstance().init(this)
                Log.d("SaveIt", "✅ youtube-dl initialized successfully!")
            }
        } catch (e: Exception) {
            Log.e("SaveIt", "❌ Failed to initialize youtube-dl", e)
        }

        // Request storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                100
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                100
            )
        }

        setContent {
            SaveItTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SaveItApp()
                }
            }
        }
    }
}

@Composable
fun SaveItApp() {
    val navController = rememberNavController()
    val viewModel = DownloadViewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDownloads = { navController.navigate("downloads") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToPlayer = { filePath ->
                    navController.navigate("player/$filePath")
                }
            )
        }
        composable("downloads") {
            DownloadsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { filePath ->
                    navController.navigate("player/$filePath")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "player/{filePath}",
            arguments = listOf(
                androidx.navigation.NavArgument("filePath") {
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            VideoPlayerScreen(
                filePath = filePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
