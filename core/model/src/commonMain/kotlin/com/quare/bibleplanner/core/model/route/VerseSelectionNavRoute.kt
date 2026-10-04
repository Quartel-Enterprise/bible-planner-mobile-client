package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/*
 * Why: the verses are not route arguments because the selection keeps changing while
 * this is on screen; they live in the shared selection store.
 */
@Serializable
data object VerseSelectionNavRoute : NavRoute
