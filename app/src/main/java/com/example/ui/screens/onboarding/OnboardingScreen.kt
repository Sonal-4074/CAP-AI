package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RoyalBlue

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightBadge: String
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: (selectedExam: String) -> Unit
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Prepare Smarter with AI",
                description = "Learn current affairs through organized news, important facts, and AI-powered explanations specifically calibrated for competitive exams.",
                icon = Icons.Default.AutoAwesome,
                highlightBadge = "AI News Analysis"
            ),
            OnboardingSlide(
                title = "Understand, Don't Just Memorize",
                description = "Explore interactive Knowledge Maps and understand intricate relationships between constitutional provisions, organizations, policies, and concepts.",
                icon = Icons.Default.Hub,
                highlightBadge = "Interactive Mind Maps"
            ),
            OnboardingSlide(
                title = "Identify Your Weaknesses",
                description = "AI analyzes your test performance, detects your weak topics with precision, and helps you achieve mastery through targeted revision and re-testing.",
                icon = Icons.Default.Insights,
                highlightBadge = "Adaptive Revision"
            )
        )
    }

    val exams = remember {
        listOf(
            "UPSC CSE",
            "SSC CGL",
            "SSC CHSL",
            "Banking (IBPS PO)",
            "Banking (SBI PO)",
            "Railway (RRB NTPC)",
            "State PSC",
            "Defence (CDS/NDA)"
        )
    }

    var currentSlide by remember { mutableIntStateOf(0) }
    var selectedExam by remember { mutableStateOf("UPSC CSE") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Cap AI Icon",
                        tint = RoyalBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAP AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = { onFinishOnboarding(selectedExam) },
                    modifier = Modifier.testTag("skip_button")
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Slide Content
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_slide"
            ) { slideIndex ->
                val slide = slides[slideIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Feature Icon Banner
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NavyDeep, RoyalBlue, IndigoPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = slide.title,
                            tint = ElectricCyan,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoyalBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = slide.highlightBadge,
                            color = RoyalBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Target Examination Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Choose Your Target Examination:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(exams) { exam ->
                            val isSelected = selectedExam == exam
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NavyDeep else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedExam = exam }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = exam,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Dot Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                slides.indices.forEach { index ->
                    val isCurrent = currentSlide == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isCurrent) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentSlide > 0) {
                    OutlinedButton(
                        onClick = { currentSlide-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Previous")
                    }
                }

                Button(
                    onClick = {
                        if (currentSlide < slides.size - 1) {
                            currentSlide++
                        } else {
                            onFinishOnboarding(selectedExam)
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentSlide > 0) 1.5f else 1f)
                        .height(52.dp)
                        .testTag("get_started_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue
                    )
                ) {
                    Text(
                        text = if (currentSlide == slides.size - 1) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
