package com.quare.bibleplanner.android.feature.main

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val whitespace = Regex("\\s+")
private const val INITIALS_FONT_RATIO = 0.38f

@Composable
internal fun MainTabAvatarImage(
    avatar: AvatarSource,
    displayName: String?,
    size: Dp,
    fallbackIcon: ImageVector,
    contentDescription: String,
) {
    val avatarModifier = Modifier
        .size(size)
        .clip(CircleShape)
        .semantics { this.contentDescription = contentDescription }
    val photoBytes = avatar.photoBytes
    val photoBitmap by rememberDecodedBitmap(photoBytes)
    val photoUrl = avatar.photoUrl
    val initials = remember(displayName) { displayName.toInitials() }
    val fallback: @Composable () -> Unit = {
        AvatarFallbackImage(
            initials = initials,
            size = size,
            fallbackIcon = fallbackIcon,
            contentDescription = contentDescription,
            modifier = avatarModifier,
        )
    }
    val bitmap = photoBitmap
    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
        )

        photoBytes == null && photoUrl != null -> SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
            loading = { fallback() },
            error = { fallback() },
        )

        else -> fallback()
    }
}

@Composable
private fun AvatarFallbackImage(
    initials: String?,
    size: Dp,
    fallbackIcon: ImageVector,
    contentDescription: String,
    modifier: Modifier,
) {
    if (initials != null) {
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
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = contentDescription,
        )
    }
}

/*
 * Why: decodes off the main thread and keeps the last decoded photo while a new one decodes, so the tab
 * icon doesn't flash back to the initials on every profile update.
 */
@Composable
private fun rememberDecodedBitmap(photoBytes: ByteArray?): State<ImageBitmap?> {
    val state = remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photoBytes) {
        state.value = photoBytes?.let { bytes ->
            withContext(Dispatchers.Default) { bytes.toImageBitmap() }
        }
    }
    return state
}

private fun ByteArray.toImageBitmap(): ImageBitmap? = BitmapFactory.decodeByteArray(this, 0, size)?.asImageBitmap()

private fun String?.toInitials(): String? {
    val words = this
        ?.trim()
        .orEmpty()
        .split(whitespace)
        .filter { it.isNotBlank() }
    if (words.isEmpty()) return null
    val first = words.first().first()
    val last = words.takeIf { it.size > 1 }?.last()?.first()
    return "$first${last ?: ""}".uppercase()
}

private fun Dp.toInitialsFontSize(): TextUnit = (value * INITIALS_FONT_RATIO).sp
