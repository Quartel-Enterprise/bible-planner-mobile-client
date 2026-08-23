package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/**
 * The verses themselves are not route arguments: the selection changes while this is on screen, as
 * the reader keeps tapping. They live in the shared selection store instead.
 */
@Serializable
data object VerseSelectionNavRoute : NavRoute
