package com.quare.bibleplanner.core.books.fake

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.DownloadOptionBuilder
import io.github.jan.supabase.storage.FileObject
import io.github.jan.supabase.storage.FileObjectV2
import io.github.jan.supabase.storage.FileUploadResponse
import io.github.jan.supabase.storage.ImageTransformation
import io.github.jan.supabase.storage.PublicUrlBuilder
import io.github.jan.supabase.storage.PurgeCacheOptions
import io.github.jan.supabase.storage.SignedUrl
import io.github.jan.supabase.storage.SignedUrlBuilder
import io.github.jan.supabase.storage.SignedUrlsBuilder
import io.github.jan.supabase.storage.StorageListFilter
import io.github.jan.supabase.storage.UploadData
import io.github.jan.supabase.storage.UploadOptionBuilder
import io.github.jan.supabase.storage.UploadSignedUrl
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.utils.io.ByteWriteChannel
import kotlin.time.Duration

internal open class ThrowingBucketApi : BucketApi {
    override val bucketId: String get() = error("Unexpected call")

    override val supabaseClient: SupabaseClient get() = error("Unexpected call")

    override val resumable: ResumableClient get() = error("Unexpected call")

    override fun setHeader(
        name: String,
        value: String,
    ): BucketApi = error("Unexpected call")

    override suspend fun upload(
        path: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit,
    ): FileUploadResponse = error("Unexpected call")

    override suspend fun uploadToSignedUrl(
        path: String,
        token: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit,
    ): FileUploadResponse = error("Unexpected call")

    override suspend fun update(
        path: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit,
    ): FileUploadResponse = error("Unexpected call")

    override suspend fun delete(paths: Collection<String>) {
        error("Unexpected call")
    }

    override suspend fun move(
        from: String,
        to: String,
        destinationBucket: String?,
    ) {
        error("Unexpected call")
    }

    override suspend fun copy(
        from: String,
        to: String,
        destinationBucket: String?,
    ) {
        error("Unexpected call")
    }

    override suspend fun createSignedUploadUrl(
        path: String,
        upsert: Boolean,
    ): UploadSignedUrl = error("Unexpected call")

    override suspend fun createSignedUrl(
        path: String,
        expiresIn: Duration,
        builder: SignedUrlBuilder.() -> Unit,
    ): String = error("Unexpected call")

    override suspend fun createSignedUrls(
        expiresIn: Duration,
        paths: Collection<String>,
        builder: SignedUrlsBuilder.() -> Unit,
    ): List<SignedUrl> = error("Unexpected call")

    override suspend fun downloadAuthenticated(
        path: String,
        options: DownloadOptionBuilder.() -> Unit,
    ): ByteArray = error("Unexpected call")

    override suspend fun downloadAuthenticated(
        path: String,
        channel: ByteWriteChannel,
        options: DownloadOptionBuilder.() -> Unit,
    ) {
        error("Unexpected call")
    }

    override suspend fun downloadPublic(
        path: String,
        options: DownloadOptionBuilder.() -> Unit,
    ): ByteArray = error("Unexpected call")

    override suspend fun downloadPublic(
        path: String,
        channel: ByteWriteChannel,
        options: DownloadOptionBuilder.() -> Unit,
    ) {
        error("Unexpected call")
    }

    override suspend fun list(
        prefix: String,
        filter: StorageListFilter.Files.() -> Unit,
    ): List<FileObject> = error("Unexpected call")

    override suspend fun info(path: String): FileObjectV2 = error("Unexpected call")

    override suspend fun exists(path: String): Boolean = error("Unexpected call")

    override suspend fun changePublicStatusTo(public: Boolean) {
        error("Unexpected call")
    }

    override suspend fun purgeCache(
        path: String,
        options: PurgeCacheOptions.() -> Unit,
    ) {
        error("Unexpected call")
    }

    override fun publicUrl(
        path: String,
        builder: PublicUrlBuilder.() -> Unit,
    ): String = error("Unexpected call")

    override fun authenticatedUrl(path: String): String = error("Unexpected call")

    override fun authenticatedRenderUrl(
        path: String,
        transform: ImageTransformation.() -> Unit,
    ): String = error("Unexpected call")
}
