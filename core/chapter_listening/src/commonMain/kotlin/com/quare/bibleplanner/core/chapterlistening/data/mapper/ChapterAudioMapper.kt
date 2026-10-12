package com.quare.bibleplanner.core.chapterlistening.data.mapper

import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioManifestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioManifestSegmentDto
import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioWordModel
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

internal class ChapterAudioMapper(
    json: Json,
) {
    // Why: a verse without a heading may leave the key out instead of sending null.
    private val manifestJson = Json(json) { explicitNulls = false }

    fun parse(manifest: String): ChapterAudioManifestDto =
        manifestJson.decodeFromString(ChapterAudioManifestDto.serializer(), manifest)

    fun map(cached: CachedChapterAudio): ChapterAudioModel = map(
        audioPath = cached.audioPath,
        manifest = parse(cached.manifest),
    )

    fun map(
        audioPath: String,
        manifest: ChapterAudioManifestDto,
    ): ChapterAudioModel = ChapterAudioModel(
        audioPath = audioPath,
        titleWords = manifest.title?.let(::toWords).orEmpty(),
        verses = manifest.verses.map { verse ->
            ChapterAudioVerseModel(
                number = verse.number,
                headingStart = verse.heading?.startMs?.milliseconds,
                headingWords = verse.heading?.let(::toWords).orEmpty(),
                start = verse.startMs.milliseconds,
                end = (verse.startMs + verse.durationMs).milliseconds,
                words = toWords(
                    segmentStartMs = verse.startMs,
                    words = verse.words,
                ),
            )
        },
        duration = manifest.durationMs.milliseconds,
    )

    private fun toWords(segment: ChapterAudioManifestSegmentDto): List<ChapterAudioWordModel> = toWords(
        segmentStartMs = segment.startMs,
        words = segment.words,
    )

    private fun toWords(
        segmentStartMs: Long,
        words: List<List<Long>>?,
    ): List<ChapterAudioWordModel> = words.orEmpty().mapNotNull { word ->
        toWord(
            segmentStartMs = segmentStartMs,
            word = word,
        )
    }

    private fun toWord(
        segmentStartMs: Long,
        word: List<Long>,
    ): ChapterAudioWordModel? {
        if (word.size < WORD_FIELD_COUNT) return null
        val (offset, startMs, durationMs) = word
        val start = segmentStartMs + startMs
        return ChapterAudioWordModel(
            offset = offset.toInt(),
            start = start.milliseconds,
            end = (start + durationMs).milliseconds,
        )
    }

    private companion object {
        const val WORD_FIELD_COUNT = 3
    }
}
