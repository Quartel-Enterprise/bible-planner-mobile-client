package com.quare.bibleplanner.feature.editprofile.presentation.utils

import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_removed
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_synced
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_too_large
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_unreadable
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_updated
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileMessage
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EditProfileMessageTextTest {
    @Test
    fun `GIVEN each message WHEN mapping it THEN picks the snackbar text for it`() {
        // Given
        val messages = EditProfileMessage.entries

        // When
        val resources = messages.associateWith(EditProfileMessage::toStringResource)

        // Then
        assertEquals(
            mapOf(
                EditProfileMessage.PHOTO_SYNCED to Res.string.edit_profile_photo_synced,
                EditProfileMessage.PHOTO_REMOVED to Res.string.edit_profile_photo_removed,
                EditProfileMessage.PHOTO_UPDATED to Res.string.edit_profile_photo_updated,
                EditProfileMessage.PHOTO_UNREADABLE to Res.string.edit_profile_photo_unreadable,
                EditProfileMessage.PHOTO_TOO_LARGE to Res.string.edit_profile_photo_too_large,
            ),
            resources,
        )
    }
}
