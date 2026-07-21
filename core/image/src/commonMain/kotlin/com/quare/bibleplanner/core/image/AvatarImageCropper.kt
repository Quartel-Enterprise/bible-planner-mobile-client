package com.quare.bibleplanner.core.image

fun interface AvatarImageCropper {
    operator fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray
}
