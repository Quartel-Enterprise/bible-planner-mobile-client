package com.quare.bibleplanner.ui.utils.model

import org.jetbrains.compose.resources.StringResource

sealed interface AppSnackbarMessage {
    val isDismissible: Boolean

    data class Resource(
        val stringResource: StringResource,
        override val isDismissible: Boolean,
    ) : AppSnackbarMessage

    /*
     * Why: native Android screens read their copy from Android res, which has no StringResource,
     * so they hand the root snackbar host the text they already resolved.
     */
    data class Text(
        val text: String,
        override val isDismissible: Boolean,
    ) : AppSnackbarMessage
}
