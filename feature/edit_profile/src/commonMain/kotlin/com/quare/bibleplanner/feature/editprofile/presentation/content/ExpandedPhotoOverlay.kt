package com.quare.bibleplanner.feature.editprofile.presentation.content

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_close
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_short_camera
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_short_gallery
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_short_provider
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_short_remove
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.ui.component.DialogWindowBlurEffect
import com.quare.bibleplanner.ui.component.ProfileAvatar
import com.quare.bibleplanner.ui.component.googleLogo
import org.jetbrains.compose.resources.stringResource

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
internal fun ExpandedPhotoOverlay(
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
            .dismissOnClick(onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        val avatarSize = if (maxWidth >= expandedBreakpoint) expandedAvatarSize else compactAvatarSize
        Column(
            modifier = Modifier.consumeClicks(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            ProfileAvatar(
                photoUrl = (profile?.avatar as? AvatarSource.Remote)?.url,
                photoBytes = (profile?.avatar as? AvatarSource.Pending)?.bytes,
                displayName = profile?.displayName,
                size = avatarSize,
                modifier = Modifier.shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                if (profile?.hasProviderPhoto == true && !profile.isUsingProviderPhoto) {
                    PhotoShortcut(
                        label = stringResource(Res.string.edit_profile_photo_short_provider),
                        background = Color.White,
                        onClick = { onEvent(ProfilePhotoUiEvent.OnUseProviderPhotoClick) },
                    ) {
                        Image(
                            imageVector = googleLogo(),
                            contentDescription = null,
                            modifier = Modifier.size(shortcutIconSize),
                        )
                    }
                }
                if (isCameraAvailable) {
                    PhotoShortcut(
                        label = stringResource(Res.string.edit_profile_photo_short_camera),
                        background = neutralShortcutBackground,
                        onClick = { onEvent(ProfilePhotoUiEvent.OnTakePhotoClick) },
                    ) {
                        ShortcutIcon(icon = Icons.Default.PhotoCamera)
                    }
                }
                PhotoShortcut(
                    label = stringResource(Res.string.edit_profile_photo_short_gallery),
                    background = neutralShortcutBackground,
                    onClick = { onEvent(ProfilePhotoUiEvent.OnPickFromGalleryClick) },
                ) {
                    ShortcutIcon(icon = Icons.Default.PhotoLibrary)
                }
                if (profile?.hasVisiblePhoto == true) {
                    PhotoShortcut(
                        label = stringResource(Res.string.edit_profile_photo_short_remove),
                        background = destructiveShortcutBackground,
                        labelColor = destructiveShortcutContent,
                        onClick = { onEvent(ProfilePhotoUiEvent.OnRemovePhotoClick) },
                    ) {
                        ShortcutIcon(
                            icon = Icons.Default.Delete,
                            tint = destructiveShortcutContent,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(closeButtonSize)
                .clip(CircleShape)
                .background(closeButtonBackground)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.edit_profile_close),
                modifier = Modifier.size(shortcutIconSize),
                tint = Color.White,
            )
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
private fun PhotoShortcut(
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
private fun Modifier.dismissOnClick(onDismiss: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onDismiss,
)

@Composable
private fun Modifier.consumeClicks(): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = {},
)
