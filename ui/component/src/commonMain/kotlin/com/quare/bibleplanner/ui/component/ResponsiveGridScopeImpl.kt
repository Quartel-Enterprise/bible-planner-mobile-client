package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

internal class ResponsiveGridScopeImpl(
    private val lazyGridScope: LazyGridScope,
    override val contentMaxWidth: Dp,
) : ResponsiveGridScope {
    override fun item(
        key: Any?,
        contentType: Any?,
        span: (LazyGridItemSpanScope.() -> GridItemSpan)?,
        content: @Composable () -> Unit,
    ) {
        lazyGridScope.item(key = key, span = span, contentType = contentType) {
            content()
        }
    }

    override fun <T> items(
        items: List<T>,
        key: ((item: T) -> Any)?,
        contentType: (item: T) -> Any?,
        span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)?,
        itemContent: @Composable (item: T) -> Unit,
    ) {
        lazyGridScope.items(
            items = items,
            key = key,
            span = span,
            contentType = contentType,
        ) { item ->
            itemContent(item)
        }
    }

    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        span: (LazyGridItemSpanScope.(Int) -> GridItemSpan)?,
        itemContent: @Composable (index: Int) -> Unit,
    ) {
        lazyGridScope.items(
            count = count,
            key = key,
            span = span,
            contentType = contentType,
        ) { index ->
            itemContent(index)
        }
    }

    override fun responsiveItem(
        key: Any?,
        contentType: Any?,
        span: (LazyGridItemSpanScope.() -> GridItemSpan)?,
        content: @Composable () -> Unit,
    ) {
        lazyGridScope.item(
            key = key,
            span = span,
            contentType = contentType,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(this@ResponsiveGridScopeImpl.contentMaxWidth)) {
                    content()
                }
            }
        }
    }

    override fun <T> responsiveItems(
        items: List<T>,
        key: ((item: T) -> Any)?,
        contentType: (item: T) -> Any?,
        span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)?,
        itemContent: @Composable (item: T) -> Unit,
    ) {
        lazyGridScope.items(
            items = items,
            key = key,
            span = span,
            contentType = contentType,
        ) { item ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(this@ResponsiveGridScopeImpl.contentMaxWidth)) {
                    itemContent(item)
                }
            }
        }
    }
}
