package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

private val whitespace = Regex("\\s+")
private const val INITIALS_FONT_RATIO = 0.38f

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    photoBytes: ByteArray?,
    displayName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = Icons.Default.AccountCircle,
    onClick: (() -> Unit)? = null,
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
    var lastPhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    when {
        photoBytes != null -> {
            val bitmap = remember(photoBytes) { photoBytes.decodeToImageBitmap() }
            LaunchedEffect(bitmap) { lastPhotoBitmap = bitmap }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = avatarModifier,
            )
        }

        photoUrl != null -> {
            val placeholder = lastPhotoBitmap
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = avatarModifier,
                loading = {
                    if (placeholder == null) {
                        ShimmerBox(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                        )
                    } else {
                        Image(
                            bitmap = placeholder,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            )
        }

        else -> {
            val initials = remember(displayName) { displayName.toInitials() }
            if (initials == null) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = avatarModifier,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Box(
                    modifier = avatarModifier.background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = size.toInitialsFontSize(),
                    )
                }
            }
        }
    }
}

private fun String?.toInitials(): String? {
    val trimmed = this?.trim().orEmpty()
    val words = trimmed.split(whitespace).filter { it.isNotBlank() }
    if (words.isEmpty()) return null
    val first = words.first().first()
    val last = words.takeIf { it.size > 1 }?.last()?.first()
    return "$first${last ?: ""}".uppercase()
}

private fun Dp.toInitialsFontSize(): TextUnit = (value * INITIALS_FONT_RATIO).sp
