package com.example.balaji_earthmovers.ui.util

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Universal entity avatar that renders a saved photo (from Uri or File),
 * or falls back to a Material 3 tinted icon inside a circle container.
 */
@Composable
fun EntityAvatar(
    imageUri: String?,
    fallbackIcon: ImageVector = Icons.Default.Person,
    defaultIcon: ImageVector = fallbackIcon,
    contentDescription: String? = null,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier.size(size)
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        if (!imageUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(imageUri)
                if (Build.VERSION.SDK_INT >= 28) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val iconToUse = if (fallbackIcon != Icons.Default.Person) fallbackIcon else defaultIcon

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = modifier
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconToUse,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size / 2)
                )
            }
        }
    }
}
