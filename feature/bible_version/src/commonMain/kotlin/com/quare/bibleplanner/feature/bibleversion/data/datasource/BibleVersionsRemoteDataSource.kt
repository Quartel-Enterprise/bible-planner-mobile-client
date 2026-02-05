package com.quare.bibleplanner.feature.bibleversion.data.datasource

import com.quare.bibleplanner.feature.bibleversion.data.dto.VersionDto
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

internal class BibleVersionsRemoteDataSource(
    private val bucketApi: BucketApi,
    private val json: Json,
) {
    suspend fun getVersions(): Result<List<VersionDto>> = runCatching {
        coroutineScope {
            val folders = bucketApi.list("bible")

            folders
                .map { folder ->
                    async {
                        runCatching {
                            val path = "bible/${folder.name}/metadata.json"
                            val bytes = bucketApi.downloadPublic(path)
                            json.decodeFromString<VersionDto>(bytes.decodeToString())
                        }.getOrNull()
                    }
                }.awaitAll()
                .filterNotNull()
        }
    }
}
