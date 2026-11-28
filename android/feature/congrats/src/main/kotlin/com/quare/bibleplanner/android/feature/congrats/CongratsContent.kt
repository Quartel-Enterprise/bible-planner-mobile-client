package com.quare.bibleplanner.android.feature.congrats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiEvent
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val horizontalPadding = 24.dp
private val topPadding = 16.dp
private val bottomPadding = 32.dp
private val iconSize = 80.dp
private val iconSpacing = 24.dp
private val titleSpacing = 16.dp
private val buttonSpacing = 32.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CongratsContent(onEvent: (CongratsUiEvent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(CongratsUiEvent.OnDismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .padding(horizontal = horizontalPadding)
                .padding(
                    top = topPadding,
                    bottom = bottomPadding,
                ).verticalScroll(rememberScrollState()),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(iconSpacing))
            Text(
                text = stringResource(R.string.congrats_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(titleSpacing))
            Text(
                text = stringResource(R.string.congrats_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(buttonSpacing))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.congrats_button))
            }
        }
    }
}
