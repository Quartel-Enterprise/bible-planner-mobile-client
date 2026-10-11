package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Why: the server refuses versions outside the same Remote Config list, so the entry is hidden for them.
internal class ObserveIsChapterListeningEnabledUseCase(
    private val audioPlayer: ChapterAudioPlayer,
    private val bibleRepository: BibleRepository,
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
    private val observeStringRemoteConfig: ObserveStringRemoteConfig,
) : ObserveIsChapterListeningEnabled {
    override fun invoke(): Flow<Boolean> {
        if (!audioPlayer.isSupported) return flowOf(false)
        return combine(
            observeBooleanRemoteConfig(
                key = CHAPTER_LISTENING_ENABLED_KEY,
                default = false,
            ),
            observeStringRemoteConfig(
                key = CLOUD_VERSIONS_KEY,
                default = DEFAULT_CLOUD_VERSIONS,
            ),
            observeSelectedVersionId(),
        ) { isEnabled, cloudVersions, versionId ->
            isEnabled && versionId != null && versionId.uppercase() in toVersionIds(cloudVersions)
        }.distinctUntilChanged()
    }

    private fun observeSelectedVersionId(): Flow<String?> = bibleRepository
        .getBiblesFlow()
        .map { bibles -> bibles.find { bible -> bible.isSelected }?.version?.id }

    private fun toVersionIds(cloudVersions: String): Set<String> = cloudVersions
        .split(VERSION_SEPARATOR)
        .map { versionId -> versionId.trim().uppercase() }
        .filter(String::isNotEmpty)
        .toSet()

    private companion object {
        const val CHAPTER_LISTENING_ENABLED_KEY = "chapter_listening_enabled"
        const val CLOUD_VERSIONS_KEY = "listening_cloud_versions"
        const val DEFAULT_CLOUD_VERSIONS = "A21,ACF,ARA,NIV,ESV,KJV,WEB"
        const val VERSION_SEPARATOR = ","
    }
}
