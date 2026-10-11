package com.quare.bibleplanner.core.chapterlistening.data.datasource

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.write
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChapterAudioCacheDataSourceImplTest {
    private val genesisOne = "A21/GENESIS/1"
    private val genesisTwo = "A21/GENESIS/2"
    private val genesisThree = "A21/GENESIS/3"
    private val manifest = """{"duration_ms":1000,"verses":[]}"""
    private lateinit var directory: Path
    private var now = 0L

    @BeforeTest
    fun setUp() {
        directory = Path(Files.createTempDirectory("chapter_audio").toString(), "cache")
        now = 0L
    }

    @AfterTest
    fun tearDown() {
        java.io.File(directory.parent.toString()).deleteRecursively()
    }

    @Test
    fun `GIVEN a saved chapter WHEN finding it THEN returns its audio file and manifest`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "A21_cast_GENESIS_1", audioBytes = 100)

        // When
        val cached = dataSource.find(genesisOne)

        // Then
        assertNotNull(cached)
        assertEquals(Path(directory, "A21_cast_GENESIS_1.mp3").toString(), cached.audioPath)
        assertEquals(manifest, cached.manifest)
        assertEquals(100L, SystemFileSystem.metadataOrNull(Path(cached.audioPath))?.size)
    }

    @Test
    fun `GIVEN nothing saved WHEN finding a chapter THEN has none`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)

        // When
        val cached = dataSource.find(genesisOne)

        // Then
        assertNull(cached)
    }

    @Test
    fun `GIVEN a cached file the system deleted WHEN finding it THEN forgets the chapter`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        SystemFileSystem.delete(Path(directory, "GEN_1.mp3"))

        // When
        val cached = dataSource.find(genesisOne)

        // Then
        assertNull(cached)
        assertNull(createDataSource(maxBytes = MAX_BYTES).find(genesisOne))
    }

    @Test
    fun `GIVEN a new revision of a chapter WHEN saving it THEN replaces the old files`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1_v1", audioBytes = 100)

        // When
        dataSource.save(genesisOne, fileName = "GEN_1_v2", audioBytes = 100)

        // Then
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1_v1.mp3")))
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1_v1.json")))
        assertEquals(Path(directory, "GEN_1_v2.mp3").toString(), dataSource.find(genesisOne)?.audioPath)
    }

    @Test
    fun `GIVEN a full cache WHEN saving another chapter THEN drops the least recently played one`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = 2 * (100L + manifest.length))
        now = 1
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        now = 2
        dataSource.save(genesisTwo, fileName = "GEN_2", audioBytes = 100)
        now = 3
        dataSource.find(genesisOne)

        // When
        now = 4
        dataSource.save(genesisThree, fileName = "GEN_3", audioBytes = 100)

        // Then
        assertNotNull(dataSource.find(genesisOne))
        assertNull(dataSource.find(genesisTwo))
        assertNotNull(dataSource.find(genesisThree))
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_2.mp3")))
    }

    @Test
    fun `GIVEN a chapter played within the hour WHEN finding it again THEN keeps the index file as it was`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        val index = readIndex()

        // When
        now = 30 * 60 * 1000L
        dataSource.find(genesisOne)

        // Then
        assertEquals(index, readIndex())
    }

    @Test
    fun `GIVEN a chapter played over an hour ago WHEN finding it again THEN saves when it was played`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)

        // When
        now = 2 * 60 * 60 * 1000L
        dataSource.find(genesisOne)

        // Then
        assertTrue(readIndex().contains("\"last_used_at\":$now"))
    }

    @Test
    fun `GIVEN a saved chapter WHEN removing it THEN deletes its files`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)

        // When
        dataSource.remove(genesisOne)

        // Then
        assertNull(dataSource.find(genesisOne))
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3")))
    }

    @Test
    fun `GIVEN an unreadable index WHEN finding a chapter THEN clears the cache`() = runTest {
        // Given
        createDataSource(maxBytes = MAX_BYTES).save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        SystemFileSystem.sink(Path(directory, "index.json")).buffered().use { it.writeString("not json") }

        // When
        val cached = createDataSource(maxBytes = MAX_BYTES).find(genesisOne)

        // Then
        assertNull(cached)
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3")))
    }

    @Test
    fun `GIVEN a download that fails WHEN saving THEN leaves no partial file behind`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)

        // When
        assertFailsWith<IllegalStateException> {
            dataSource.save(
                chapterKey = genesisOne,
                fileName = "GEN_1",
                manifest = manifest,
                writeAudio = { destination ->
                    SystemFileSystem.sink(destination).buffered().use { it.writeString("half") }
                    error("connection lost")
                },
            )
        }

        // Then
        assertTrue(SystemFileSystem.list(directory).isEmpty())
        assertNull(dataSource.find(genesisOne))
    }

    private fun readIndex(): String = java.io.File(directory.toString(), "index.json").readText()

    private suspend fun ChapterAudioCacheDataSource.save(
        chapterKey: String,
        fileName: String,
        audioBytes: Int,
    ) {
        save(
            chapterKey = chapterKey,
            fileName = fileName,
            manifest = manifest,
            writeAudio = { destination ->
                SystemFileSystem.sink(destination).buffered().use { it.write(ByteArray(audioBytes)) }
            },
        )
    }

    private fun createDataSource(maxBytes: Long): ChapterAudioCacheDataSource = ChapterAudioCacheDataSourceImpl(
        directory = ChapterAudioCacheDirectory(directory),
        maxBytes = maxBytes,
        json = Json { ignoreUnknownKeys = true },
        currentTimestampProvider = { now },
    )

    private companion object {
        const val MAX_BYTES = 10L * 1024 * 1024
    }
}
