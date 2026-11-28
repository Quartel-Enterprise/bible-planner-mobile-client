package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel

private val cardCorner = 16.dp
private val dividerInset = 70.dp
private const val OTHER_VERSIONS_KEY = "other-versions"

@Composable
internal fun AnnotationsList(
    groups: List<AnnotationGroupUiModel>,
    otherVersions: List<OtherVersionAnnotationsUiModel>,
    openMenuItemKey: String?,
    isWide: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        if (otherVersions.isNotEmpty()) {
            item(key = OTHER_VERSIONS_KEY) {
                AnnotationsOtherVersionsCard(
                    otherVersions = otherVersions,
                    isEmptyState = false,
                    onEvent = onEvent,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
        groups.forEachIndexed { groupIndex, group ->
            item(key = "header-$groupIndex-${group.label}") {
                Text(
                    text = group.label.text(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        top = if (groupIndex == 0) 4.dp else 18.dp,
                        bottom = 8.dp,
                    ),
                )
            }
            itemsIndexed(
                items = group.items,
                key = { _, item -> item.key },
            ) { index, item ->
                Surface(
                    shape = createSegmentShape(
                        index = index,
                        lastIndex = group.items.lastIndex,
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = dividerInset),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        AnnotationItemRow(
                            item = item,
                            isMenuOpen = item.key == openMenuItemKey,
                            isWide = isWide,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

private fun createSegmentShape(
    index: Int,
    lastIndex: Int,
): Shape = RoundedCornerShape(
    topStart = if (index == 0) cardCorner else 0.dp,
    topEnd = if (index == 0) cardCorner else 0.dp,
    bottomStart = if (index == lastIndex) cardCorner else 0.dp,
    bottomEnd = if (index == lastIndex) cardCorner else 0.dp,
)
