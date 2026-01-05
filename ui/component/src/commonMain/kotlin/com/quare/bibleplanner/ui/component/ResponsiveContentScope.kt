package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * A scope for [ResponsiveContent] that provides functions to create responsive, centered items
 * within a [LazyListScope].
 */
interface ResponsiveContentScope : LazyListScope {
    /**
     * The maximum width that the content should occupy.
     */
    val contentMaxWidth: Dp

    /**
     * Adds a single item to the [LazyListScope] that is centered and constrained to [contentMaxWidth].
     *
     * @param key a factory of stable and unique keys for the item.
     * @param contentType a factory of the content type for the item.
     * @param content the content of the item.
     */
    fun responsiveItem(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable () -> Unit,
    )

    /**
     * Adds a list of items to the [LazyListScope] where each item is centered and constrained to [contentMaxWidth].
     *
     * @param items the data list.
     * @param key a factory of stable and unique keys for each item.
     * @param contentType a factory of the content type for each item.
     * @param itemContent the content of each item.
     */
    fun <T> responsiveItems(
        items: List<T>,
        key: ((item: T) -> Any)? = null,
        contentType: (item: T) -> Any? = { null },
        itemContent: @Composable (item: T) -> Unit,
    )
}
