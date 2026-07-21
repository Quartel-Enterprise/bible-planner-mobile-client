package com.quare.bibleplanner.feature.editprofile.domain.usecase

fun interface UpdateDisplayName {
    suspend operator fun invoke(displayName: String)
}
