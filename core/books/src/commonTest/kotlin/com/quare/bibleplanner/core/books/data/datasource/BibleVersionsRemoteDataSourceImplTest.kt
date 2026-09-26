package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.fake.ThrowingBucketApi
import io.github.jan.supabase.storage.DownloadOptionBuilder
import io.github.jan.supabase.storage.FileObject
import io.github.jan.supabase.storage.StorageListFilter
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class BibleVersionsRemoteDataSourceImplTest {
    private lateinit var dataSource: BibleVersionsRemoteDataSourceImpl
    private lateinit var bucketApi: FakeBucketApi

    @Test
    fun `GIVEN version folders WHEN fetching the versions THEN decodes each folder metadata`() = runTest {
        // Given
        prepareScenario(
            folders = listOf("WEB", "ACF"),
            metadataByFolder = mapOf(
                "WEB" to metadata(id = "WEB"),
                "ACF" to metadata(id = "ACF"),
            ),
        )

        // When
        val result = dataSource.getVersions()

        // Then
        assertEquals(listOf("WEB", "ACF"), result.getOrThrow().map(VersionDto::id))
        assertEquals(listOf("bible"), bucketApi.listedPrefixes)
        assertEquals(
            setOf("bible/WEB/metadata.json", "bible/ACF/metadata.json"),
            bucketApi.downloadedPaths.toSet(),
        )
    }

    @Test
    fun `GIVEN a folder whose metadata fails to load WHEN fetching the versions THEN skips only that folder`() =
        runTest {
            // Given
            prepareScenario(
                folders = listOf("WEB", "BROKEN", "ACF"),
                metadataByFolder = mapOf(
                    "WEB" to metadata(id = "WEB"),
                    "BROKEN" to "{ not json",
                    "ACF" to metadata(id = "ACF"),
                ),
            )

            // When
            val result = dataSource.getVersions()

            // Then
            assertEquals(listOf("WEB", "ACF"), result.getOrThrow().map(VersionDto::id))
        }

    @Test
    fun `GIVEN the listing fails WHEN fetching the versions THEN returns the failure`() = runTest {
        // Given
        prepareScenario(
            folders = null,
            metadataByFolder = emptyMap(),
        )

        // When
        val result = dataSource.getVersions()

        // Then
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    private fun metadata(id: String): String =
        """{"id":"$id","name":"$id name","version":"1.0.0","language":"en","country":"us","chapters":1189,"size":null}"""

    private fun prepareScenario(
        folders: List<String>?,
        metadataByFolder: Map<String, String>,
    ) {
        bucketApi = FakeBucketApi(
            folders = folders,
            metadataByFolder = metadataByFolder,
        )
        dataSource = BibleVersionsRemoteDataSourceImpl(
            bucketApi = bucketApi,
            json = Json { ignoreUnknownKeys = true },
        )
    }
}

private class FakeBucketApi(
    private val folders: List<String>?,
    private val metadataByFolder: Map<String, String>,
) : ThrowingBucketApi() {
    val listedPrefixes = mutableListOf<String>()
    val downloadedPaths = mutableListOf<String>()

    override suspend fun list(
        prefix: String,
        filter: StorageListFilter.Files.() -> Unit,
    ): List<FileObject> {
        listedPrefixes += prefix
        return checkNotNull(folders) { "listing failed" }.map { name ->
            FileObject(
                name = name,
                id = null,
                updatedAt = null,
                createdAt = null,
                lastAccessedAt = null,
                metadata = null,
            )
        }
    }

    override suspend fun downloadPublic(
        path: String,
        options: DownloadOptionBuilder.() -> Unit,
    ): ByteArray {
        downloadedPaths += path
        val folder = path.removePrefix("bible/").substringBefore("/")
        return metadataByFolder.getValue(folder).encodeToByteArray()
    }
}
