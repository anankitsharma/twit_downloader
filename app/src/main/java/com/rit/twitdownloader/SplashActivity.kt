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
    
    // Gradient background colors like the example
    val gradientStart = Color(0xFF2D1B1B) // Dark brown-black
    val gradientEnd = Color(0xFF3D2B2B) // Lighter brown
    
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(gradientStart, gradientEnd)
    )
    
    val progressColor = Color(0xFF1DA1F2) // Twitter blue
    val progressTrackColor = Color(0xFF4A3A3A) // Dark brown
    
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
            .background(backgroundGradient)
    ) {
        // Decorative background elements like the example
        VideoDecorativeElements()
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon with black background like the example
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .padding(20.dp)
                    .alpha(alpha)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App name
            Text(
                text = "X Video Downloader",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "One-click Fast Download",
                fontSize = 14.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
        }
        
        // Progress bar at bottom like the example
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .padding(horizontal = 32.dp)
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = progressTrackColor
            )
        }
    }
}

@Composable
fun VideoDecorativeElements() {
    // Popcorn bucket - top left (like the example)
    Text(
        text = "🍿",
        fontSize = 48.sp,
        modifier = Modifier
            .offset(x = 40.dp, y = 80.dp)
            .alpha(0.15f)
    )
    
    // Play button - top right (like the example)
    Text(
        text = "▶️",
        fontSize = 44.sp,
        modifier = Modifier
            .offset(x = 280.dp, y = 100.dp)
            .alpha(0.12f)
    )
    
    // Film strip - bottom right (like the example)
    Text(
        text = "🎬",
        fontSize = 40.sp,
        modifier = Modifier
            .offset(x = 300.dp, y = 500.dp)
            .alpha(0.1f)
    )
    
    // Video camera - bottom left
    Text(
        text = "📹",
        fontSize = 36.sp,
        modifier = Modifier
            .offset(x = 30.dp, y = 450.dp)
            .alpha(0.08f)
    )
    
    // Download arrow - center left
    Text(
        text = "⬇️",
        fontSize = 32.sp,
        modifier = Modifier
            .offset(x = 20.dp, y = 300.dp)
            .alpha(0.06f)
    )
    
    // Link icon - center right
    Text(
        text = "🔗",
        fontSize = 28.sp,
        modifier = Modifier
            .offset(x = 320.dp, y = 350.dp)
            .alpha(0.07f)
    )
}

