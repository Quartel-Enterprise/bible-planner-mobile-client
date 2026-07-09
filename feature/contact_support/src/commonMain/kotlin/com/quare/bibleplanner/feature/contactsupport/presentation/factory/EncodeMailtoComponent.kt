package com.quare.bibleplanner.feature.contactsupport.presentation.factory

internal fun interface EncodeMailtoComponent {
    operator fun invoke(value: String): String
}
