package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

interface ResponsiveGridScope {
    val contentMaxWidth: Dp

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        span: (LazyGridItemSpanScope.() -> GridItemSpan)? = null,
        content: @Composable () -> Unit,
    )

    fun <T> items(
        items: List<T>,
        key: ((item: T) -> Any)? = null,
        contentType: (item: T) -> Any? = { null },
        span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
        itemContent: @Composable (item: T) -> Unit,
    )

    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        span: (LazyGridItemSpanScope.(Int) -> GridItemSpan)? = null,
        itemContent: @Composable (index: Int) -> Unit,
    )

    fun responsiveItem(
        key: Any? = null,
        contentType: Any? = null,
        span: (LazyGridItemSpanScope.() -> GridItemSpan)? = null,
        content: @Composable () -> Unit,
    )

    fun <T> responsiveItems(
        items: List<T>,
        key: ((item: T) -> Any)? = null,
        contentType: (item: T) -> Any? = { null },
        span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
        itemContent: @Composable (item: T) -> Unit,
    )
}
