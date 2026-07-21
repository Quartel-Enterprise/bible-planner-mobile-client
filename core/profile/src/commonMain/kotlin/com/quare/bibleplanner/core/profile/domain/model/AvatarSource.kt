package com.quare.bibleplanner.core.profile.domain.model

sealed interface AvatarSource {
    data class Remote(
        val url: String,
    ) : AvatarSource

    class Pending(
        val bytes: ByteArray,
    ) : AvatarSource {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Pending) return false
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data object None : AvatarSource
}
