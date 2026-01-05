package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

internal class ResponsiveContentScopeImpl(
    private val lazyListScope: LazyListScope,
    override val contentMaxWidth: Dp,
) : ResponsiveContentScope,
    LazyListScope by lazyListScope {
    override fun responsiveItem(
        key: Any?,
        contentType: Any?,
        content: @Composable () -> Unit,
    ) {
        lazyListScope.centeredItem(contentMaxWidth, key, contentType, content)
    }

    override fun <T> responsiveItems(
        items: List<T>,
        key: ((item: T) -> Any)?,
        contentType: (item: T) -> Any?,
        itemContent: @Composable (item: T) -> Unit,
    ) {
        lazyListScope.items(
            items = items,
            key = key,
            contentType = contentType,
        ) { item ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(this@ResponsiveContentScopeImpl.contentMaxWidth)) {
                    itemContent(item)
                }
            }
        }
    }

    /**
     * Helper to wrap an item in a centered box with a max width.
     */
    private fun LazyListScope.centeredItem(
        contentMaxWidth: Dp,
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable () -> Unit,
    ) {
        item(
            key = key,
            contentType = contentType,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(contentMaxWidth)) {
                    content()
                }
            }
        }
    }
}
