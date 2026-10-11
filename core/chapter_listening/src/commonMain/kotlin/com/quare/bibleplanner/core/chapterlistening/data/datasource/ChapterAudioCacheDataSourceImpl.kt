package com.quare.bibleplanner.core.chapterlistening.data.datasource

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioCacheEntryDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioCacheIndexDto
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioStorageException
import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.utils.ioDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

/*
 * Why: the files are immutable per URL, so the cache only needs an index of which chapter each pair
 * of files belongs to and when it was last played, to drop the least recently played ones first.
 */
internal class ChapterAudioCacheDataSourceImpl(
    private val directory: ChapterAudioCacheDirectory,
    private val maxBytes: Long,
    private val json: Json,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ChapterAudioCacheDataSource {
    private val touchWriteInterval: Duration = 1.hours
    private val mutex = Mutex()
    private val indexPath = Path(directory.path, INDEX_FILE_NAME)
    private var loadedEntries: List<ChapterAudioCacheEntryDto>? = null

    override suspend fun find(chapterKey: String): CachedChapterAudio? = withContext(ioDispatcher) {
        mutex.withLock {
            val entries = loadEntries()
            val entry = entries.find { it.chapterKey == chapterKey } ?: return@withLock null
            val audioPath = toAudioPath(entry.fileName)
            val manifestPath = toManifestPath(entry.fileName)
            if (!SystemFileSystem.exists(audioPath) || !SystemFileSystem.exists(manifestPath)) {
                writeEntries(entries - entry)
                return@withLock null
            }
            touch(
                entries = entries,
                entry = entry,
            )
            CachedChapterAudio(
                fileName = entry.fileName,
                audioPath = audioPath.toString(),
                manifest = readText(manifestPath),
            )
        }
    }

    override suspend fun save(
        chapterKey: String,
        fileName: String,
        manifest: String,
        writeAudio: suspend (Path) -> Unit,
    ): CachedChapterAudio = withContext(ioDispatcher) {
        store { SystemFileSystem.createDirectories(directory.path) }
        val partialPath = Path(directory.path, "$fileName$PARTIAL_EXTENSION")
        var isWritten = false
        // Why: a cancelled or failed download must not leave a partial file that is never indexed.
        try {
            writeAudio(partialPath)
            isWritten = true
        } finally {
            if (!isWritten) SystemFileSystem.delete(partialPath, mustExist = false)
        }
        mutex.withLock {
            val audioPath = toAudioPath(fileName)
            val manifestPath = toManifestPath(fileName)
            // Why: files that never make it into the index could never be evicted.
            try {
                store {
                    SystemFileSystem.atomicMove(partialPath, audioPath)
                    writeText(
                        path = manifestPath,
                        text = manifest,
                    )
                }
            } catch (exception: ChapterAudioStorageException) {
                listOf(partialPath, audioPath, manifestPath).forEach { path ->
                    SystemFileSystem.delete(path, mustExist = false)
                }
                throw exception
            }
            val entry = ChapterAudioCacheEntryDto(
                chapterKey = chapterKey,
                fileName = fileName,
                bytes = sizeOf(audioPath) + sizeOf(manifestPath),
                lastUsedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
            // Why: a replaced revision may still be playing, so its files stay until the next launch drops them.
            val others = loadEntries().filter { it.chapterKey != chapterKey }
            val kept = findEntriesToKeep(
                entries = others,
                reservedBytes = entry.bytes,
            )
            (others - kept.toSet()).forEach(::deleteFiles)
            store { writeEntries(kept + entry) }
            CachedChapterAudio(
                fileName = fileName,
                audioPath = audioPath.toString(),
                manifest = manifest,
            )
        }
    }

    override suspend fun remove(chapterKey: String) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val (removed, kept) = loadEntries().partition { it.chapterKey == chapterKey }
                removed.forEach(::deleteFiles)
                writeEntries(kept)
            }
        }
    }

    // Why: the order only matters to evict, so a play inside the same hour doesn't rewrite the index.
    private fun touch(
        entries: List<ChapterAudioCacheEntryDto>,
        entry: ChapterAudioCacheEntryDto,
    ) {
        val now = currentTimestampProvider.getCurrentTimestamp()
        val touched = entries.map { if (it == entry) it.copy(lastUsedAt = now) else it }
        if ((now - entry.lastUsedAt).milliseconds >= touchWriteInterval) {
            writeEntries(touched)
        } else {
            loadedEntries = touched
        }
    }

    // Why: a full disk must read as a storage failure, not as the missing connection a download error means.
    private inline fun <T> store(block: () -> T): T = try {
        block()
    } catch (exception: IOException) {
        throw ChapterAudioStorageException(exception)
    }

    private fun findEntriesToKeep(
        entries: List<ChapterAudioCacheEntryDto>,
        reservedBytes: Long,
    ): List<ChapterAudioCacheEntryDto> {
        var totalBytes = reservedBytes
        return entries
            .sortedByDescending(ChapterAudioCacheEntryDto::lastUsedAt)
            .filter { entry ->
                totalBytes += entry.bytes
                totalBytes <= maxBytes
            }
    }

    private fun loadEntries(): List<ChapterAudioCacheEntryDto> =
        loadedEntries ?: readEntries().also { loadedEntries = it }

    /*
     * Why: the index is read once per launch, by the find that precedes every download, so no file
     * is being written or played yet; files it doesn't know about (replaced revisions, downloads cut
     * short by the app being killed, everything when it can't be read) could never be evicted.
     */
    private fun readEntries(): List<ChapterAudioCacheEntryDto> {
        if (!SystemFileSystem.exists(indexPath)) return emptyList()
        val entries = try {
            json.decodeFromString(ChapterAudioCacheIndexDto.serializer(), readText(indexPath)).entries
        } catch (exception: SerializationException) {
            Logger.w(exception) { "Unreadable chapter audio cache index, clearing the cache" }
            emptyList()
        }
        val knownNames = entries
            .flatMap { entry ->
                listOf(toAudioPath(entry.fileName).name, toManifestPath(entry.fileName).name)
            }.toSet() + INDEX_FILE_NAME
        SystemFileSystem
            .list(directory.path)
            .filter { path -> path.name !in knownNames }
            .forEach { path -> SystemFileSystem.delete(path, mustExist = false) }
        return entries
    }

    private fun writeEntries(entries: List<ChapterAudioCacheEntryDto>) {
        loadedEntries = entries
        SystemFileSystem.createDirectories(directory.path)
        val partialPath = Path(directory.path, "$INDEX_FILE_NAME$PARTIAL_EXTENSION")
        writeText(
            path = partialPath,
            text = json.encodeToString(ChapterAudioCacheIndexDto.serializer(), ChapterAudioCacheIndexDto(entries)),
        )
        SystemFileSystem.atomicMove(partialPath, indexPath)
    }

    private fun deleteFiles(entry: ChapterAudioCacheEntryDto) {
        SystemFileSystem.delete(toAudioPath(entry.fileName), mustExist = false)
        SystemFileSystem.delete(toManifestPath(entry.fileName), mustExist = false)
    }

    private fun toAudioPath(fileName: String): Path = Path(directory.path, "$fileName$AUDIO_EXTENSION")

    private fun toManifestPath(fileName: String): Path = Path(directory.path, "$fileName$MANIFEST_EXTENSION")

    private fun sizeOf(path: Path): Long = SystemFileSystem.metadataOrNull(path)?.size ?: 0L

    private fun readText(path: Path): String = SystemFileSystem.source(path).buffered().use { it.readString() }

    private fun writeText(
        path: Path,
        text: String,
    ) {
        SystemFileSystem.sink(path).buffered().use { it.writeString(text) }
    }

    private companion object {
        const val INDEX_FILE_NAME = "index.json"
        const val AUDIO_EXTENSION = ".mp3"
        const val MANIFEST_EXTENSION = ".json"
        const val PARTIAL_EXTENSION = ".part"
    }
}
