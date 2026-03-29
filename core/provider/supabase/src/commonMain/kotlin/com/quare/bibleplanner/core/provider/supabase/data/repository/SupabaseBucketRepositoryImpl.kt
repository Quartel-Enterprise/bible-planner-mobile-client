package com.quare.bibleplanner.core.provider.supabase.data.repository

import com.quare.bibleplanner.core.provider.supabase.domain.repository.SupabaseBucketRepository
import io.github.jan.supabase.storage.BucketApi

class SupabaseBucketRepositoryImpl(
    private val bucketApi: BucketApi,
): SupabaseBucketRepository {
    override suspend fun getByteArrayResult(filePath: String): Result<ByteArray> = runCatching {
        bucketApi.downloadPublic(filePath)
    }
}
