package com.quare.bibleplanner.feature.editprofile.presentation.utils

import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_removed
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_synced
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_too_large
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_unreadable
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_updated
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileMessage
import org.jetbrains.compose.resources.StringResource

internal fun EditProfileMessage.toStringResource(): StringResource = when (this) {
    EditProfileMessage.PHOTO_SYNCED -> Res.string.edit_profile_photo_synced
    EditProfileMessage.PHOTO_REMOVED -> Res.string.edit_profile_photo_removed
    EditProfileMessage.PHOTO_UPDATED -> Res.string.edit_profile_photo_updated
    EditProfileMessage.PHOTO_UNREADABLE -> Res.string.edit_profile_photo_unreadable
    EditProfileMessage.PHOTO_TOO_LARGE -> Res.string.edit_profile_photo_too_large
}
