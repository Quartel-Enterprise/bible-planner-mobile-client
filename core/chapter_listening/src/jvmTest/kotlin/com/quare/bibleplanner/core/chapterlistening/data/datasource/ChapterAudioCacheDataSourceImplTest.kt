package com.quare.bibleplanner.core.chapterlistening.data.datasource

import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioStorageException
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
    fun `GIVEN a manifest the system purged WHEN finding its chapter THEN deletes the audio left behind`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        SystemFileSystem.delete(Path(directory, "GEN_1.json"))

        // When
        val cached = dataSource.find(genesisOne)

        // Then
        assertNull(cached)
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3")))
    }

    @Test
    fun `GIVEN a replaced revision removed after failing WHEN saving another chapter THEN stops counting it`() =
        runTest {
            // Given
            val dataSource = createDataSource(maxBytes = 3 * (100L + manifest.length))
            now = 1
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
            now = 2
            dataSource.save(genesisTwo, fileName = "GEN_2_v1", audioBytes = 100)
            dataSource.save(genesisTwo, fileName = "GEN_2_v2", audioBytes = 100)
            dataSource.remove(Path(directory, "GEN_2_v1.mp3").toString())

            // When
            now = 3
            dataSource.save(genesisThree, fileName = "GEN_3", audioBytes = 100)

            // Then
            assertNotNull(dataSource.find(genesisOne))
        }

    @Test
    fun `GIVEN a new revision of a chapter WHEN saving it THEN serves it and keeps the playing file`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1_v1", audioBytes = 100)

        // When
        dataSource.save(genesisOne, fileName = "GEN_1_v2", audioBytes = 100)

        // Then
        assertTrue(SystemFileSystem.exists(Path(directory, "GEN_1_v1.mp3")))
        assertEquals(Path(directory, "GEN_1_v2.mp3").toString(), dataSource.find(genesisOne)?.audioPath)
    }

    @Test
    fun `GIVEN a replaced revision still on disk WHEN saving another chapter THEN counts its bytes`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = 3 * (100L + manifest.length))
        now = 1
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        now = 2
        dataSource.save(genesisTwo, fileName = "GEN_2_v1", audioBytes = 100)
        dataSource.save(genesisTwo, fileName = "GEN_2_v2", audioBytes = 100)

        // When
        now = 3
        dataSource.save(genesisThree, fileName = "GEN_3", audioBytes = 100)

        // Then
        assertNull(dataSource.find(genesisOne))
        assertNotNull(dataSource.find(genesisTwo))
    }

    @Test
    fun `GIVEN a replaced revision and a cut download WHEN the next launch reads the cache THEN deletes them`() =
        runTest {
            // Given
            val dataSource = createDataSource(maxBytes = MAX_BYTES)
            dataSource.save(genesisOne, fileName = "GEN_1_v1", audioBytes = 100)
            dataSource.save(genesisOne, fileName = "GEN_1_v2", audioBytes = 100)
            SystemFileSystem.sink(Path(directory, "GEN_2.mp3.part")).buffered().use { it.writeString("half") }

            // When
            val cached = createDataSource(maxBytes = MAX_BYTES).find(genesisOne)

            // Then
            assertEquals(Path(directory, "GEN_1_v2.mp3").toString(), cached?.audioPath)
            assertEquals(
                setOf("GEN_1_v2.mp3", "GEN_1_v2.json", "index.json"),
                SystemFileSystem.list(directory).map(Path::name).toSet(),
            )
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
    fun `GIVEN an index that cannot be saved WHEN finding a chapter played long ago THEN still serves it`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        SystemFileSystem.createDirectories(Path(directory, "index.json.part"))
        now = 2 * 60 * 60 * 1000L

        // When
        val cached = dataSource.find(genesisOne)

        // Then
        assertNotNull(cached)
    }

    @Test
    fun `GIVEN a chapter replayed every half hour WHEN an hour passed since the last save THEN saves the play`() =
        runTest {
            // Given
            val dataSource = createDataSource(maxBytes = MAX_BYTES)
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
            now = 30 * 60 * 1000L
            dataSource.find(genesisOne)

            // When
            now = 70 * 60 * 1000L
            dataSource.find(genesisOne)

            // Then
            assertTrue(readIndex().contains("\"last_used_at\":$now"))
        }

    @Test
    fun `GIVEN files left without an index WHEN the next launch reads the cache THEN deletes them`() = runTest {
        // Given
        SystemFileSystem.createDirectories(directory)
        SystemFileSystem.sink(Path(directory, "GEN_1.mp3.part")).buffered().use { it.writeString("half") }

        // When
        val cached = createDataSource(maxBytes = MAX_BYTES).find(genesisOne)

        // Then
        assertNull(cached)
        assertTrue(SystemFileSystem.list(directory).isEmpty())
    }

    @Test
    fun `GIVEN an index that cannot be written WHEN saving THEN does not report the chapter as cached`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.find(genesisTwo)
        SystemFileSystem.createDirectories(Path(directory, "index.json.part"))

        // When
        assertFailsWith<ChapterAudioStorageException> {
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        }

        // Then
        assertNull(dataSource.find(genesisOne))
    }

    @Test
    fun `GIVEN a saved chapter WHEN removing it THEN deletes its files`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)

        // When
        dataSource.remove(Path(directory, "GEN_1.mp3").toString())

        // Then
        assertNull(dataSource.find(genesisOne))
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3")))
    }

    @Test
    fun `GIVEN a replaced revision that fails WHEN removing it THEN keeps the new one`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.save(genesisOne, fileName = "GEN_1_v1", audioBytes = 100)
        dataSource.save(genesisOne, fileName = "GEN_1_v2", audioBytes = 100)

        // When
        dataSource.remove(Path(directory, "GEN_1_v1.mp3").toString())

        // Then
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1_v1.mp3")))
        assertEquals(Path(directory, "GEN_1_v2.mp3").toString(), dataSource.find(genesisOne)?.audioPath)
    }

    @Test
    fun `GIVEN a large recent chapter over the budget WHEN saving THEN keeps the older ones that still fit`() =
        runTest {
            // Given
            val dataSource = createDataSource(maxBytes = 450L + 2 * manifest.length)
            now = 1
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
            now = 2
            dataSource.save(genesisTwo, fileName = "GEN_2", audioBytes = 300)

            // When
            now = 3
            dataSource.save(genesisThree, fileName = "GEN_3", audioBytes = 250)

            // Then
            assertNotNull(dataSource.find(genesisOne))
            assertNull(dataSource.find(genesisTwo))
        }

    @Test
    fun `GIVEN a chapter played over an hour ago WHEN looking up its file name THEN leaves when it was played`() =
        runTest {
            // Given
            val dataSource = createDataSource(maxBytes = MAX_BYTES)
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
            val index = readIndex()
            now = 2 * 60 * 60 * 1000L

            // When
            val fileName = dataSource.findFileName(genesisOne)

            // Then
            assertEquals("GEN_1", fileName)
            assertEquals(index, readIndex())
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
                readManifest = { manifest },
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

    @Test
    fun `GIVEN a manifest that cannot be written WHEN saving THEN leaves no audio file behind`() = runTest {
        // Given
        val dataSource = createDataSource(maxBytes = MAX_BYTES)
        dataSource.find(genesisTwo)
        SystemFileSystem.createDirectories(Path(directory, "GEN_1.json"))

        // When
        assertFailsWith<ChapterAudioStorageException> {
            dataSource.save(genesisOne, fileName = "GEN_1", audioBytes = 100)
        }

        // Then
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3")))
        assertFalse(SystemFileSystem.exists(Path(directory, "GEN_1.mp3.part")))
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
            readManifest = { manifest },
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
