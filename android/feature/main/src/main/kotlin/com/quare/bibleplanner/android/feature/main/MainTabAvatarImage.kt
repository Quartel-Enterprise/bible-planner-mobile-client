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
import coil3.compose.AsyncImage
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl

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
    val photoBitmap = remember(photoBytes) { photoBytes?.toImageBitmap() }
    val photoUrl = avatar.photoUrl
    val initials = remember(displayName) { displayName.toInitials() }
    when {
        photoBitmap != null -> Image(
            bitmap = photoBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
        )

        photoUrl != null -> AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
        )

        initials != null -> Box(
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

        else -> Icon(
            imageVector = fallbackIcon,
            contentDescription = contentDescription,
        )
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
    val first = words.first().first()
    val last = words.takeIf { it.size > 1 }?.last()?.first()
    return "$first${last ?: ""}".uppercase()
}

private fun Dp.toInitialsFontSize(): TextUnit = (value * INITIALS_FONT_RATIO).sp
