package com.quare.bibleplanner.core.chapterlistening.data.mapper

import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioManifestDto
import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
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
        verses = manifest.verses.map { verse ->
            ChapterAudioVerseModel(
                number = verse.number,
                headingStart = verse.heading?.startMs?.milliseconds,
                start = verse.startMs.milliseconds,
                end = (verse.startMs + verse.durationMs).milliseconds,
            )
        },
        duration = manifest.durationMs.milliseconds,
    )
}
