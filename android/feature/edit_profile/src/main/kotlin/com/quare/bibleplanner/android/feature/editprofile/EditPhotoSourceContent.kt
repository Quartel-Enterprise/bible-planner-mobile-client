package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditPhotoSourceContent(
    profile: UserProfile?,
    isCameraAvailable: Boolean,
    onDismiss: () -> Unit,
    onEvent: (ProfilePhotoUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close(onDismiss) },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 8.dp,
                ),
                text = stringResource(R.string.edit_profile_photo_title),
                style = MaterialTheme.typography.titleLarge,
            )
            if (profile?.hasProviderPhoto == true && !profile.isUsingProviderPhoto) {
                PhotoSourceItem(
                    icon = Icons.Rounded.AccountCircle,
                    text = stringResource(R.string.edit_profile_photo_use_provider),
                    // Why: the ViewModel pops the sheet when the photo changes; a second tap would pop the screen below.
                    onClick = { sheetCloseGuard.close { onEvent(ProfilePhotoUiEvent.OnUseProviderPhotoClick) } },
                )
            }
            PhotoSourceItem(
                icon = Icons.Rounded.PhotoLibrary,
                text = stringResource(R.string.edit_profile_photo_gallery),
                onClick = { onEvent(ProfilePhotoUiEvent.OnPickFromGalleryClick) },
            )
            if (isCameraAvailable) {
                PhotoSourceItem(
                    icon = Icons.Rounded.PhotoCamera,
                    text = stringResource(R.string.edit_profile_photo_camera),
                    onClick = { onEvent(ProfilePhotoUiEvent.OnTakePhotoClick) },
                )
            }
            if (profile?.hasVisiblePhoto == true) {
                PhotoSourceItem(
                    icon = Icons.Rounded.Delete,
                    text = stringResource(R.string.edit_profile_photo_remove),
                    onClick = { sheetCloseGuard.close { onEvent(ProfilePhotoUiEvent.OnRemovePhotoClick) } },
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun PhotoSourceItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color? = null,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = text) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        colors = if (color == null) {
            ListItemDefaults.colors(containerColor = Color.Transparent)
        } else {
            ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = color,
                leadingIconColor = color,
            )
        },
    )
}
