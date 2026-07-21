package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_camera
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_gallery
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_remove
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_use_provider
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PhotoSourceSheetContent(
    profile: UserProfile?,
    isCameraAvailable: Boolean,
    onEvent: (ProfilePhotoUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp),
    ) {
        if (profile?.hasProviderPhoto == true && !profile.isUsingProviderPhoto) {
            EditProfileRow(
                icon = Icons.Default.AccountCircle,
                text = stringResource(Res.string.edit_profile_photo_use_provider),
                onClick = { onEvent(ProfilePhotoUiEvent.OnUseProviderPhotoClick) },
            )
        }
        EditProfileRow(
            icon = Icons.Default.PhotoLibrary,
            text = stringResource(Res.string.edit_profile_photo_gallery),
            onClick = { onEvent(ProfilePhotoUiEvent.OnPickFromGalleryClick) },
        )
        if (isCameraAvailable) {
            EditProfileRow(
                icon = Icons.Default.PhotoCamera,
                text = stringResource(Res.string.edit_profile_photo_camera),
                onClick = { onEvent(ProfilePhotoUiEvent.OnTakePhotoClick) },
            )
        }
        if (profile?.hasVisiblePhoto == true) {
            EditProfileRow(
                icon = Icons.Default.Delete,
                text = stringResource(Res.string.edit_profile_photo_remove),
                onClick = { onEvent(ProfilePhotoUiEvent.OnRemovePhotoClick) },
                tint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
