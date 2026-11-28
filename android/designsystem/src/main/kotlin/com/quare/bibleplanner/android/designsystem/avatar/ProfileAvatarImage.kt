package com.quare.bibleplanner.android.designsystem.avatar

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage

private val whitespace = Regex("\\s+")
private const val INITIALS_FONT_RATIO = 0.38f

@Composable
fun ProfileAvatarImage(
    photoUrl: String?,
    photoBytes: ByteArray?,
    displayName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
    // Why: keeps the previous photo on screen while a new URL loads, instead of flashing the placeholder.
    var lastPhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val photoBitmap = remember(photoBytes) { photoBytes?.toImageBitmap() }
    when {
        photoBitmap != null -> {
            LaunchedEffect(photoBitmap) { lastPhotoBitmap = photoBitmap }
            Image(
                bitmap = photoBitmap,
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
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
                error = {
                    InitialsAvatarBox(
                        displayName = displayName,
                        size = size,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        }

        else -> InitialsAvatarBox(
            displayName = displayName,
            size = size,
            modifier = avatarModifier,
        )
    }
}

@Composable
private fun InitialsAvatarBox(
    displayName: String?,
    size: Dp,
    modifier: Modifier,
) {
    val initials = remember(displayName) { displayName.toInitials() }
    if (initials == null) {
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
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

private fun ByteArray.toImageBitmap(): ImageBitmap? = BitmapFactory.decodeByteArray(this, 0, size)?.asImageBitmap()

private fun String?.toInitials(): String? {
    val words = this
        ?.trim()
        .orEmpty()
        .split(whitespace)
        .filter { it.isNotBlank() }
    if (words.isEmpty()) return null
    val first = words.first().toFirstCharacter()
    val last = words.takeIf { it.size > 1 }?.last()?.toFirstCharacter()
    return "$first${last.orEmpty()}".uppercase()
}

// Why: a name starting with an emoji or another supplementary character would otherwise keep half a surrogate pair.
private fun String.toFirstCharacter(): String = take(Character.charCount(codePointAt(0)))

private fun Dp.toInitialsFontSize(): TextUnit = (value * INITIALS_FONT_RATIO).sp
