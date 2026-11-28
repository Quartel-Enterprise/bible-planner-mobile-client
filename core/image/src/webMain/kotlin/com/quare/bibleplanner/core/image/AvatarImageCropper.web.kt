package com.quare.bibleplanner.core.image

actual fun createAvatarImageCropper(): AvatarImageCropper = AvatarImageCropper { _, _ ->
    throw UnsupportedImageFormatException()
}
