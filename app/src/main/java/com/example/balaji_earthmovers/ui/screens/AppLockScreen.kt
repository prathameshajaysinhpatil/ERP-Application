package com.example.balaji_earthmovers.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balaji_earthmovers.R
import com.example.balaji_earthmovers.ui.theme.ExpenseRed
import com.example.balaji_earthmovers.ui.theme.ProfitGreen
import com.example.balaji_earthmovers.ui.util.LocalStrings
import com.example.balaji_earthmovers.ui.viewmodel.BalajiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    viewModel: BalajiViewModel,
    onUnlocked: () -> Unit
) {
    val strings = LocalStrings.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessUnlocked by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val unlockScale = remember { Animatable(1f) }
    val unlockAlpha = remember { Animatable(1f) }
    val lockIconRotation = remember { Animatable(0f) }

    fun handleKeyPress(key: String) {
        if (isSuccessUnlocked) return
        if (enteredPin.length < 4) {
            val newPin = enteredPin + key
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (viewModel.verifyAppPin(newPin)) {
                    isSuccessUnlocked = true
                    coroutineScope.launch {
                        launch {
                            lockIconRotation.animateTo(-15f, tween(120, easing = FastOutSlowInEasing))
                            lockIconRotation.animateTo(0f, tween(160, easing = FastOutSlowInEasing))
                        }
                        launch {
                            unlockScale.animateTo(1.06f, tween(180, easing = FastOutSlowInEasing))
                            unlockScale.animateTo(0.96f, tween(240, easing = FastOutSlowInEasing))
                        }
                        launch {
                            delay(220)
                            unlockAlpha.animateTo(0f, tween(220, easing = LinearEasing))
                        }
                        delay(460)
                        onUnlocked()
                    }
                } else {
                    errorMessage = strings.incorrectPin
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        if (isSuccessUnlocked) return
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .scale(unlockScale.value)
            .alpha(unlockAlpha.value)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Official Balaji Earthmovers Logo
            Image(
                painter = painterResource(id = R.drawable.logo_balaji_main),
                contentDescription = strings.brandFullName,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.brandFullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isSuccessUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isSuccessUnlocked) ProfitGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(lockIconRotation.value)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isSuccessUnlocked) "Access Granted" else strings.enterAppPin,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSuccessUnlocked) ProfitGreen else MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Just "Enter 4 digit code to open" without any default PIN text
            Text(
                text = strings.enter4DigitCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 4 Dots for PIN indication
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor = when {
                        isSuccessUnlocked -> ProfitGreen
                        isFilled -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(if (isSuccessUnlocked) 18.dp else 16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = 1.dp,
                                color = if (isSuccessUnlocked) ProfitGreen else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage!!,
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (key in row) {
                            when (key) {
                                "" -> {
                                    Spacer(modifier = Modifier.size(68.dp))
                                }
                                "DEL" -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable { handleBackspace() }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        tonalElevation = 1.dp,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable { handleKeyPress(key) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
