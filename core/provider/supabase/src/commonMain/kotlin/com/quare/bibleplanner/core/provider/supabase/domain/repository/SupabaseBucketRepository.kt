package com.quare.bibleplanner.core.provider.supabase.domain.repository

fun interface SupabaseBucketRepository {
    suspend fun getByteArrayResult(filePath: String): Result<ByteArray>
}
