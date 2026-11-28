package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Badge
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
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiEvent
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProfileContent(
    onDismiss: () -> Unit,
    onEvent: (EditProfileUiEvent) -> Unit,
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
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.titleLarge,
            )
            // Why: each option replaces this sheet with the next screen; a second tap would replace that screen too.
            EditProfileItem(
                icon = Icons.Rounded.Badge,
                text = stringResource(R.string.edit_profile_change_name),
                onClick = { sheetCloseGuard.close { onEvent(EditProfileUiEvent.OnChangeNameClick) } },
            )
            EditProfileItem(
                icon = Icons.Rounded.AddAPhoto,
                text = stringResource(R.string.edit_profile_change_image),
                onClick = { sheetCloseGuard.close { onEvent(EditProfileUiEvent.OnChangeImageClick) } },
            )
        }
    }
}

@Composable
private fun EditProfileItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
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
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
