package com.quare.bibleplanner.android.feature.editprofile

import androidx.annotation.StringRes
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileMessage

@StringRes
internal fun EditProfileMessage.toStringRes(): Int = when (this) {
    EditProfileMessage.PHOTO_SYNCED -> R.string.edit_profile_photo_synced
    EditProfileMessage.PHOTO_REMOVED -> R.string.edit_profile_photo_removed
    EditProfileMessage.PHOTO_UPDATED -> R.string.edit_profile_photo_updated
    EditProfileMessage.PHOTO_UNREADABLE -> R.string.edit_profile_photo_unreadable
    EditProfileMessage.PHOTO_TOO_LARGE -> R.string.edit_profile_photo_too_large
}
