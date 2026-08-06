package com.quare.bibleplanner.feature.deleteaccount.presentation.provider

internal fun interface GetConfirmationKeyword {
    suspend operator fun invoke(): String
}
