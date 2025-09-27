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
    var progress by remember { mutableStateOf(0f) }
    var showContent by remember { mutableStateOf(false) }
    
    // Blue and black color scheme
    val appBlue = Color(0xFF1DA1F2) // Twitter blue
    val appDarkBlue = Color(0xFF0F1419) // Very dark blue-black
    val appMidBlue = Color(0xFF1A2332) // Medium dark blue
    val appLightBlue = Color(0xFF2A3F5F) // Lighter blue
    val appTextLight = Color(0xFFB0BEC5) // Light blue-grey text
    
    // Background gradient - like the example but with blue tones
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            appDarkBlue,    // Dark blue-black at top
            appMidBlue,     // Medium dark blue in middle
            appLightBlue    // Lighter blue at bottom
        )
    )
    
    // Animate progress bar continuously (like a snake)
    LaunchedEffect(Unit) {
        showContent = true
        delay(100)
        
        // Continuous progress animation
        while (true) {
            progress = 0f
            repeat(100) {
                progress += 0.01f
                delay(30) // Smooth animation
            }
            delay(200) // Brief pause before restart
        }
    }
    
    // Auto-finish after 2 seconds (faster)
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Background emoji decorations - like the example
        VideoEmojiBackground()
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon - using the actual app logo with black background like the example
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App name
            Text(
                text = "X Video Downloader",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "Single Click Fast Download",
                fontSize = 16.sp,
                color = appTextLight,
                textAlign = TextAlign.Center
            )
        }
        
        // Progress bar at bottom - like the example
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .padding(horizontal = 32.dp)
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = appBlue,
                trackColor = appTextLight.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun VideoEmojiBackground() {
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
