package com.quare.bibleplanner.core.books.data.datasource

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.data.dto.VersionsManifestDto
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

internal class BibleVersionsRemoteDataSourceImpl(
    private val bucketApi: BucketApi,
    private val json: Json,
) : BibleVersionsRemoteDataSource {
    override suspend fun getVersions(): Result<List<VersionDto>> =
        fetchManifestVersionsOrNull()?.let(Result.Companion::success) ?: fetchVersionsByListing()

    /*
     * Why: one request instead of listing the bucket, which makes Storage search every chapter file's
     * name to find the version folders, and then downloading each metadata.json. bible-versions
     * publishes the manifest after every metadata.json and the CDN may keep it five minutes, so right
     * after a bump it can still name the previous content version; a download started then just gets
     * that version's files and is offered the update later.
     */
    private suspend fun fetchManifestVersionsOrNull(): List<VersionDto>? = suspendRunCatching {
        val bytes = bucketApi.downloadPublic(MANIFEST_PATH)
        val versions = json.decodeFromString<VersionsManifestDto>(bytes.decodeToString()).versions
        check(versions.isNotEmpty()) { "$MANIFEST_PATH lists no versions" }
        versions
    }.onFailure { Logger.w(it) { "Could not read $MANIFEST_PATH, listing the Bible versions instead" } }
        .getOrNull()

    private suspend fun fetchVersionsByListing(): Result<List<VersionDto>> = suspendRunCatching {
        coroutineScope {
            val folders = bucketApi.list("bible")

            folders
                .map { folder ->
                    async {
                        suspendRunCatching {
                            val path = "bible/${folder.name}/metadata.json"
                            val bytes = bucketApi.downloadPublic(path)
                            json.decodeFromString<VersionDto>(bytes.decodeToString())
                        }.onFailure { Logger.e(it) { "Failed to load version metadata for ${folder.name}" } }
                            .getOrNull()
                    }
                }.awaitAll()
                .filterNotNull()
        }
    }.onFailure { Logger.e(it) { "Failed to list the remote Bible versions" } }

    private companion object {
        const val MANIFEST_PATH = "versions.json"
    }
}
