package com.quare.bibleplanner.core.image

const val MAX_AVATAR_SOURCE_BYTES = 15 * 1024 * 1024
const val AVATAR_OUTPUT_SIZE_PX = 512
const val AVATAR_JPEG_QUALITY = 80

expect fun avatarImageCropper(): AvatarImageCropper
