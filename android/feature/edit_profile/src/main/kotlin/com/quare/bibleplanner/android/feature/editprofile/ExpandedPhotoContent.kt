package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.avatar.ProfileAvatarImage
import com.quare.bibleplanner.android.designsystem.dialog.DialogWindowBlurEffect
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent

private val compactAvatarSize = 200.dp
private val expandedAvatarSize = 260.dp
private val shortcutSize = 58.dp
private val shortcutLabelWidth = 76.dp
private val shortcutCornerRadius = 12.dp
private val shortcutIconSize = 26.dp
private val closeButtonSize = 44.dp
private val expandedBreakpoint = 600.dp
private val backdropBlurRadius = 18.dp
private val backdropColor = Color(0xFF08080E).copy(alpha = 0.28f)
private val closeButtonBackground = Color.Black.copy(alpha = 0.32f)
private val neutralShortcutBackground = Color.White.copy(alpha = 0.16f)
private val destructiveShortcutBackground = Color(0xFFFF7878).copy(alpha = 0.22f)
private val destructiveShortcutContent = Color(0xFFFF9A9A)

@Composable
internal fun ExpandedPhotoContent(
    profile: UserProfile?,
    isCameraAvailable: Boolean,
    onDismiss: () -> Unit,
    onEvent: (ProfilePhotoUiEvent) -> Unit,
) {
    DialogWindowBlurEffect(radius = backdropBlurRadius)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backdropColor)
            .clickableWithoutIndication(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        val avatarSize = if (maxWidth >= expandedBreakpoint) expandedAvatarSize else compactAvatarSize
        Column(
            modifier = Modifier.clickableWithoutIndication(onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            ProfileAvatarImage(
                photoUrl = profile?.avatar?.photoUrl,
                photoBytes = profile?.avatar?.photoBytes,
                displayName = profile?.displayName,
                size = avatarSize,
                modifier = Modifier.shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                ),
            )
            PhotoShortcutRow(
                profile = profile,
                isCameraAvailable = isCameraAvailable,
                onEvent = onEvent,
            )
        }
        FilledIconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(closeButtonSize),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = closeButtonBackground,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.edit_profile_close),
                modifier = Modifier.size(shortcutIconSize),
            )
        }
    }
}

@Composable
private fun PhotoShortcutRow(
    profile: UserProfile?,
    isCameraAvailable: Boolean,
    onEvent: (ProfilePhotoUiEvent) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        if (profile?.hasProviderPhoto == true && !profile.isUsingProviderPhoto) {
            PhotoShortcutButton(
                label = stringResource(R.string.edit_profile_photo_short_provider),
                background = Color.White,
                onClick = { onEvent(ProfilePhotoUiEvent.OnUseProviderPhotoClick) },
            ) {
                Image(
                    painter = painterResource(R.drawable.edit_profile_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(shortcutIconSize),
                )
            }
        }
        if (isCameraAvailable) {
            PhotoShortcutButton(
                label = stringResource(R.string.edit_profile_photo_short_camera),
                background = neutralShortcutBackground,
                onClick = { onEvent(ProfilePhotoUiEvent.OnTakePhotoClick) },
            ) {
                ShortcutIcon(icon = Icons.Rounded.PhotoCamera)
            }
        }
        PhotoShortcutButton(
            label = stringResource(R.string.edit_profile_photo_short_gallery),
            background = neutralShortcutBackground,
            onClick = { onEvent(ProfilePhotoUiEvent.OnPickFromGalleryClick) },
        ) {
            ShortcutIcon(icon = Icons.Rounded.PhotoLibrary)
        }
        if (profile?.hasVisiblePhoto == true) {
            PhotoShortcutButton(
                label = stringResource(R.string.edit_profile_photo_short_remove),
                background = destructiveShortcutBackground,
                labelColor = destructiveShortcutContent,
                onClick = { onEvent(ProfilePhotoUiEvent.OnRemovePhotoClick) },
            ) {
                ShortcutIcon(
                    icon = Icons.Rounded.Delete,
                    tint = destructiveShortcutContent,
                )
            }
        }
    }
}

@Composable
private fun ShortcutIcon(
    icon: ImageVector,
    tint: Color = Color.White,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(shortcutIconSize),
        tint = tint,
    )
}

@Composable
private fun PhotoShortcutButton(
    label: String,
    background: Color,
    onClick: () -> Unit,
    labelColor: Color = Color.White,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(shortcutLabelWidth)
            .clip(RoundedCornerShape(shortcutCornerRadius))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(shortcutSize)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Modifier.clickableWithoutIndication(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick,
)
