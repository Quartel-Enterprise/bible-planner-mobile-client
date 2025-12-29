package com.quare.bibleplanner.feature.onboardingstartdate.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SetDateHeader(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SetDateIconsHeader(
            onCloseClick = onCloseClick,
        )
        SetDateHeaderText(
            modifier = Modifier.padding(16.dp),
        )
    }
}
