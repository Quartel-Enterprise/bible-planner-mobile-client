package com.quare.bibleplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * A scope for [ResponsiveColumn] that provides functions to create responsive, centered items.
 */
interface ResponsiveContentScope {
    /**
     * The maximum width that the content should occupy.
     */
    val contentMaxWidth: Dp

    /**
     * Adds a single item that is not necessarily centered.
     */
    fun item(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable () -> Unit,
    )

    /**
     * Adds a list of items that are not necessarily centered.
     */
    fun <T> items(
        items: List<T>,
        key: ((item: T) -> Any)? = null,
        contentType: (item: T) -> Any? = { null },
        itemContent: @Composable (item: T) -> Unit,
    )

    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: @Composable (index: Int) -> Unit,
    )

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
