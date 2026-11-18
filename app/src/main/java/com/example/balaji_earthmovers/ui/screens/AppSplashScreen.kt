package com.example.balaji_earthmovers.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balaji_earthmovers.R
import com.example.balaji_earthmovers.ui.theme.BrandGoldPrimary
import com.example.balaji_earthmovers.ui.theme.BrandGoldSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced Microsoft 365 style Motion Splash Screen:
 * 1. Displays the Tilak symbol emblem (Image 2) with a smooth zoom, subtle angle rotation & golden glow.
 * 2. Morph/Cross-fades fluidly into the full official Balaji Earthmovers branding (Image 1).
 * 3. Text and executive badge slide up smoothly with elevated depth.
 * Fast, crisp, and high-performance (~1.8s total).
 */
@Composable
fun AppSplashScreen(
    onSplashFinished: () -> Unit
) {
    // Stage 1: Tilak Emblem Icon Animators
    val iconScale = remember { Animatable(0.62f) }
    val iconAlpha = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(-5f) }

    // Stage 2: Full Official Logo Animators
    val fullLogoScale = remember { Animatable(0.90f) }
    val fullLogoAlpha = remember { Animatable(0f) }

    // Glow Effect
    val glowScale = remember { Animatable(0.7f) }
    val glowAlpha = remember { Animatable(0f) }

    // Stage 3: Title & Subtitle Entrance Animators
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(18f) }

    LaunchedEffect(Unit) {
        // Step 1: Tilak Emblem Icon Entrance (Paced & Graceful with golden glow)
        launch {
            glowAlpha.animateTo(0.75f, animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing))
            glowScale.animateTo(1.18f, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
        }
        launch {
            iconRotation.animateTo(0f, animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing))
        }
        launch {
            iconAlpha.animateTo(1f, animationSpec = tween(durationMillis = 650, easing = LinearEasing))
            iconScale.animateTo(1.04f, animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing))
        }

        delay(850)

        // Step 2: Fluid cross-fade & morph into Full Brand Logo
        launch {
            iconAlpha.animateTo(0f, animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing))
            iconScale.animateTo(1.15f, animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing))
        }
        launch {
            fullLogoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
            fullLogoScale.animateTo(1f, animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing))
        }

        delay(600)

        // Step 3: Typography & Owner badge reveal with smooth slide up
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing))
            textOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing))
        }

        delay(1400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Golden Glow Halo behind the logo
        if (glowAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(glowScale.value)
                    .alpha(glowAlpha.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrandGoldSecondary.copy(alpha = 0.35f),
                                BrandGoldPrimary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Image 2: Tilak Emblem (Start of motion)
        if (iconAlpha.value > 0f) {
            Image(
                painter = painterResource(id = R.drawable.logo_balaji_icon),
                contentDescription = "Balaji Icon",
                modifier = Modifier
                    .size(136.dp)
                    .scale(iconScale.value)
                    .rotate(iconRotation.value)
                    .alpha(iconAlpha.value)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(16.dp, RoundedCornerShape(24.dp))
            )
        }

        // Image 1: Official Full Logo (Transited final state with text reveal)
        if (fullLogoAlpha.value > 0f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .scale(fullLogoScale.value)
                    .alpha(fullLogoAlpha.value)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, BrandGoldPrimary.copy(alpha = 0.3f)),
                    shadowElevation = 14.dp,
                    color = Color.Transparent
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_balaji_full),
                        contentDescription = "Balaji Earthmovers Official Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = textOffsetY.value.dp)
                        .alpha(textAlpha.value)
                ) {
                    Text(
                        text = "Balaji Earthmovers Kodoli",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Owner: Ajaysinh Jaysing Patil",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
