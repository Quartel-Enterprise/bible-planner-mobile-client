package com.quare.bibleplanner.feature.editprofile.domain.usecase

fun interface SetProfilePhoto {
    suspend operator fun invoke(bytes: ByteArray)
}
