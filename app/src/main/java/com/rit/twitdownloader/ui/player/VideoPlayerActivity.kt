package com.rit.twitdownloader.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.rit.twitdownloader.ui.theme.SealTheme
import com.rit.twitdownloader.util.FileUtil
import com.rit.twitdownloader.util.ToastUtil
import kotlinx.coroutines.launch
import java.io.File

class VideoPlayerActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        private const val TAG = "VideoPlayerActivity"
    }
    
    private val viewModel: VideoPlayerViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ExoPlayer
        viewModel.initializePlayer(this)
        
        val videoUri = intent.getStringExtra(EXTRA_VIDEO_URI)
        if (videoUri.isNullOrEmpty()) {
            Log.e(TAG, "No video URI provided")
            showErrorAndFinish("No video file provided")
            return
        }
        
        val uri = try {
            Uri.parse(videoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid video URI: $videoUri", e)
            showErrorAndFinish("Invalid video file")
            return
        }
        
        // Check if file exists and is accessible
        if (!isVideoFileAccessible(uri)) {
            Log.e(TAG, "Video file not accessible: $videoUri")
            showErrorWithFallback("Video file not found or inaccessible")
            return
        }
        
        viewModel.setVideoUri(uri)
        
        setContent {
            SealTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() },
                        onError = { error ->
                            // Handle error in non-composable context
                            lifecycleScope.launch {
                                showErrorWithFallback(error)
                            }
                        }
                    )
                }
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        viewModel.pausePlayer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }
    
    private fun isVideoFileAccessible(uri: Uri): Boolean {
        return try {
            when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path ?: "")
                    file.exists() && file.canRead()
                }
                "content" -> {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        cursor.moveToFirst()
                    } != null
                }
                else -> false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception accessing video file", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking video file accessibility", e)
            false
        }
    }
    
    private fun showErrorAndFinish(message: String) {
        ToastUtil.makeToast(message)
        finish()
    }
    
    private fun showErrorWithFallback(message: String) {
        ToastUtil.makeToast("$message. Opening with external app...")
        lifecycleScope.launch {
            try {
                val videoUri = intent.getStringExtra(EXTRA_VIDEO_URI)
                if (!videoUri.isNullOrEmpty()) {
                    FileUtil.openFile(videoUri) {
                        ToastUtil.makeToast("Unable to open video file")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening with external app", e)
                ToastUtil.makeToast("Unable to open video file")
            }
            finish()
        }
    }
}
