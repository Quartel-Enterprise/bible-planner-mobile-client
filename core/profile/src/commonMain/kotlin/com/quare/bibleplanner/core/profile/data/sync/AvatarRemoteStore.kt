package com.quare.bibleplanner.core.profile.data.sync

import io.github.jan.supabase.storage.BucketApi

internal class AvatarRemoteStore(
    private val bucketApi: BucketApi,
) {
    suspend fun upload(
        userId: String,
        bytes: ByteArray,
    ): String {
        val path = pathOf(userId)
        bucketApi.upload(path = path, data = bytes) { upsert = true }
        return bucketApi.publicUrl(path)
    }

    suspend fun delete(userId: String) {
        bucketApi.delete(pathOf(userId))
    }

    private fun pathOf(userId: String): String = "$userId/$AVATAR_FILE_NAME"

    private companion object {
        const val AVATAR_FILE_NAME = "avatar.jpg"
    }
}
