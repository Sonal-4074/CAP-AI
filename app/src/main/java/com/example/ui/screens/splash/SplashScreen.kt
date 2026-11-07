package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val glowAlpha = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        glowAlpha.animateTo(
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(2200L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, NavyDark, Color(0xFF060B18))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Subtle glowing aura
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    RoyalBlue.copy(alpha = glowAlpha.value),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // App Icon Asset
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_1794034553667),
                    contentDescription = "CAP AI Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CAP AI",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Current Affairs Preparation with AI",
                color = ElectricCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "UPSC • SSC • Banking • State PSC",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
