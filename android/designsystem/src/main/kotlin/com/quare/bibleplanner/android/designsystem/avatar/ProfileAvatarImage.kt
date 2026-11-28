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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
    // Why: hashing walks every byte of the photo, so it runs once per array instead of on every recomposition.
    val photoBytesHash = remember(photoBytes) { photoBytes?.contentHashCode() }
    val targetSizePx = with(LocalDensity.current) { size.roundToPx() }
    var decodedPhoto by remember { mutableStateOf<DecodedPhoto?>(null) }
    // Why: keeps the previous photo on screen while new bytes decode or a new URL loads, instead of the placeholder.
    var lastPhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    // Why: keyed on the content hash, so a profile update that copies the same bytes doesn't decode them again.
    LaunchedEffect(
        photoBytesHash,
        targetSizePx,
    ) {
        if (photoBytes == null || photoBytesHash == null) return@LaunchedEffect
        val bitmap = withContext(Dispatchers.Default) { photoBytes.toSampledImageBitmap(targetSizePx) }
        decodedPhoto = DecodedPhoto(
            bytesHash = photoBytesHash,
            bitmap = bitmap,
        )
        if (bitmap != null) lastPhotoBitmap = bitmap
    }
    val isDecoding = photoBytesHash != null && decodedPhoto?.bytesHash != photoBytesHash
    val photoBitmap = decodedPhoto?.takeIf { it.bytesHash == photoBytesHash }?.bitmap
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

        isDecoding && placeholder != null -> AvatarBitmapImage(
            bitmap = placeholder,
            modifier = avatarModifier,
        )

        photoUrl != null -> SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
            loading = {
                AvatarLoadingImage(
                    placeholder = placeholder,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            error = { fallback(Modifier.fillMaxSize()) },
        )

        isDecoding -> AvatarLoadingImage(
            placeholder = null,
            modifier = avatarModifier,
        )

        else -> fallback(avatarModifier)
    }
}

private class DecodedPhoto(
    val bytesHash: Int,
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
private fun AvatarLoadingImage(
    placeholder: ImageBitmap?,
    modifier: Modifier,
) {
    if (placeholder == null) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest))
    } else {
        AvatarBitmapImage(
            bitmap = placeholder,
            modifier = modifier,
        )
    }
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

// Why: a full-resolution camera photo would hold megabytes of pixels for an avatar a few dozen pixels wide.
private fun ByteArray.toSampledImageBitmap(targetSizePx: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(
        this,
        0,
        size,
        bounds,
    )
    val options = BitmapFactory.Options().apply {
        inSampleSize = getSampleSize(
            shorterSidePx = minOf(bounds.outWidth, bounds.outHeight),
            targetSizePx = targetSizePx,
        )
    }
    return BitmapFactory
        .decodeByteArray(
            this,
            0,
            size,
            options,
        )?.asImageBitmap()
}

// Why: the photo is cropped to a circle, so its shorter side must still cover the target after sampling.
private fun getSampleSize(
    shorterSidePx: Int,
    targetSizePx: Int,
): Int {
    var sampleSize = 1
    while (targetSizePx > 0 && shorterSidePx / (sampleSize * 2) >= targetSizePx) {
        sampleSize *= 2
    }
    return sampleSize
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
