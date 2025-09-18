package com.rit.twitdownloader.ui.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoPlayerViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "VideoPlayerViewModel"
    }
    
    private var _playerState = MutableStateFlow(VideoPlayerState())
    val playerState: StateFlow<VideoPlayerState> = _playerState.asStateFlow()
    
    lateinit var exoPlayer: ExoPlayer
        private set
    
    fun initializePlayer(context: Context) {
        if (!::exoPlayer.isInitialized) {
            exoPlayer = ExoPlayer.Builder(context).build()
            
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "Player error: ${error.message}", error)
                    _playerState.value = _playerState.value.copy(
                        error = "Playback error: ${error.message}"
                    )
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = isPlaying
                    )
                }
            })
        }
    }
    
    fun setVideoUri(uri: Uri) {
        if (::exoPlayer.isInitialized) {
            try {
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                
                _playerState.value = _playerState.value.copy(
                    error = null,
                    isPlaying = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error setting video URI: $uri", e)
                _playerState.value = _playerState.value.copy(
                    error = "Failed to load video: ${e.message}"
                )
            }
        }
    }
    
    fun pausePlayer() {
        if (::exoPlayer.isInitialized) {
            exoPlayer.pause()
        }
    }
    
    fun releasePlayer() {
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
    }
}

data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val error: String? = null
)


