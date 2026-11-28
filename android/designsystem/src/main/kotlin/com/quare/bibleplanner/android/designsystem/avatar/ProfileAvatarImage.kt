package com.quare.bibleplanner.android.designsystem.avatar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val whitespace = Regex("\\s+")
private const val INITIALS_FONT_RATIO = 0.38f

@Composable
fun ProfileAvatarImage(
    photoUrl: String?,
    photoBytes: ByteArray?,
    displayName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = Icons.Rounded.AccountCircle,
    fallbackIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
    val targetSizePx = with(LocalDensity.current) { size.roundToPx() }
    // Why: a new avatar for bytes another one already decoded, like the tab icon's, shows the photo on its first frame.
    val cachedBitmap = remember(photoBytes) { photoBytes?.let(DecodedAvatarPhotoCache::findOrNull)?.bitmap }
    var decodedPhoto by remember { mutableStateOf<DecodedPhoto?>(null) }
    // Why: keeps the previous photo on screen while new bytes decode or a new URL loads, instead of the fallback.
    var lastPhotoBitmap by remember { mutableStateOf(cachedBitmap) }
    LaunchedEffect(
        photoBytes,
        targetSizePx,
    ) {
        if (photoBytes == null) return@LaunchedEffect
        val bitmap = DecodedAvatarPhotoCache.findCoveringOrNull(
            bytes = photoBytes,
            targetSizePx = targetSizePx,
        ) ?: withContext(Dispatchers.Default) {
            DecodedAvatarPhotoCache.decode(
                bytes = photoBytes,
                targetSizePx = targetSizePx,
            )
        }
        decodedPhoto = DecodedPhoto(
            bytes = photoBytes,
            bitmap = bitmap,
        )
        if (bitmap != null) lastPhotoBitmap = bitmap
    }
    val isDecoding = photoBytes != null && decodedPhoto?.bytes !== photoBytes
    val photoBitmap = decodedPhoto?.takeIf { it.bytes === photoBytes }?.bitmap
        ?: cachedBitmap
        ?: lastPhotoBitmap.takeIf { isDecoding }
    val placeholder = lastPhotoBitmap
    val fallback: @Composable (Modifier) -> Unit = { fallbackModifier ->
        InitialsAvatarBox(
            displayName = displayName,
            size = size,
            fallbackIcon = fallbackIcon,
            fallbackIconTint = fallbackIconTint,
            modifier = fallbackModifier,
        )
    }
    when {
        photoBitmap != null -> AvatarBitmapImage(
            bitmap = photoBitmap,
            modifier = avatarModifier,
        )

        photoUrl != null -> SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
            loading = {
                if (placeholder == null) {
                    fallback(Modifier.fillMaxSize())
                } else {
                    AvatarBitmapImage(
                        bitmap = placeholder,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
            error = { fallback(Modifier.fillMaxSize()) },
        )

        else -> fallback(avatarModifier)
    }
}

// Why: bytes are compared by reference, so a profile update that keeps the same array doesn't decode it again.
private class DecodedPhoto(
    val bytes: ByteArray,
    val bitmap: ImageBitmap?,
)

@Composable
private fun AvatarBitmapImage(
    bitmap: ImageBitmap,
    modifier: Modifier,
) {
    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
private fun InitialsAvatarBox(
    displayName: String?,
    size: Dp,
    fallbackIcon: ImageVector,
    fallbackIconTint: Color,
    modifier: Modifier,
) {
    val initials = remember(displayName) { displayName.toInitials() }
    if (initials == null) {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            modifier = modifier,
            tint = fallbackIconTint,
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
