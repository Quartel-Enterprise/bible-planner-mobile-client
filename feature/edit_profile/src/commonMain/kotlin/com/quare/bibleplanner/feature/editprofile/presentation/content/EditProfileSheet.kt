package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_change_image
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_change_name
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditProfileSheetContent(onEvent: (EditProfileUiEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp),
    ) {
        EditProfileRow(
            icon = Icons.Default.Badge,
            text = stringResource(Res.string.edit_profile_change_name),
            onClick = { onEvent(EditProfileUiEvent.OnChangeNameClick) },
        )
        EditProfileRow(
            icon = Icons.Default.AddAPhoto,
            text = stringResource(Res.string.edit_profile_change_image),
            onClick = { onEvent(EditProfileUiEvent.OnChangeImageClick) },
        )
    }
}
