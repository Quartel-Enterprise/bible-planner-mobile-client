package com.quare.bibleplanner.core.chapterlistening.data.mapper

import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioManifestDto
import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

internal class ChapterAudioMapper(
    private val json: Json,
) {
    fun map(cached: CachedChapterAudio): ChapterAudioModel {
        val manifest = json.decodeFromString(ChapterAudioManifestDto.serializer(), cached.manifest)
        return ChapterAudioModel(
            audioPath = cached.audioPath,
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
}
