package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)

@Composable
internal fun MoreScreen(
    items: List<MoreMenuItemPresentationModel>,
    onEvent: (MoreUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items) { index, item ->
            MoreMenuItem(
                itemModel = item,
                onEvent = onEvent,
            )
            if (index < items.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    itemModel: MoreMenuItemPresentationModel,
    onEvent: (event: MoreUiEvent) -> Unit,
) {
    itemModel.run {
        val text = stringResource(name)
        ListItem(
            headlineContent = { Text(text) },
            leadingContent = { Icon(imageVector = icon, contentDescription = text) },
            modifier = Modifier.clickable(onClick = { onEvent(event) }),
        )
    }
}
