package com.rit.twitdownloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pre-warm the main activity
        val intent = Intent(this, MainActivity::class.java)
        
        setContent {
            SplashScreen(
                onSplashFinished = {
                    // Navigate to MainActivity after splash
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var scale by remember { mutableStateOf(0.8f) }
    var alpha by remember { mutableStateOf(0f) }
    var progress by remember { mutableStateOf(0f) }
    
    // Simple dark background
    val backgroundColor = Color(0xFF0F1419)
    val progressColor = Color(0xFF1DA1F2) // Twitter blue
    val progressTrackColor = Color(0xFF2A3F5F) // Dark blue
    
    // Logo and progress animation
    LaunchedEffect(Unit) {
        // Fade in and scale up animation
        alpha = 1f
        scale = 1f
        
        // Progress animation
        repeat(100) {
            progress += 0.01f
            delay(12) // Smooth progress animation
        }
        
        // Auto-finish after progress completes
        delay(200)
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bigger logo with animation
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(160.dp) // Bigger logo
                    .alpha(alpha)
                    .scale(scale)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Modern progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = progressTrackColor
            )
        }
    }
}

