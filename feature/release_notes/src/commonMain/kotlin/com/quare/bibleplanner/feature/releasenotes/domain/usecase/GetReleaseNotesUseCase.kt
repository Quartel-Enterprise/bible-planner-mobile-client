package com.quare.bibleplanner.feature.releasenotes.domain.usecase

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.domain.repository.ReleaseNotesRepository

class GetReleaseNotesUseCase(
    private val releaseNotesRepository: ReleaseNotesRepository,
) {
    suspend operator fun invoke(): Result<List<ReleaseNoteModel>> = releaseNotesRepository
        .getReleaseNotes()
        .map { notes ->
            notes.sortedWith { a, b -> compareVersions(b.version, a.version) }
        }

    private fun compareVersions(
        v1: String,
        v2: String,
    ): Int {
        val parts1 = v1.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
