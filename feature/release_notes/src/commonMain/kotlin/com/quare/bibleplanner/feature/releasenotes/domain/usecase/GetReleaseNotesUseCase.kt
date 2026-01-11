package com.quare.bibleplanner.feature.releasenotes.domain.usecase

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.domain.repository.ReleaseNotesRepository
import com.quare.bibleplanner.feature.releasenotes.domain.utils.VersionComparator

class GetReleaseNotesUseCase(
    private val releaseNotesRepository: ReleaseNotesRepository,
) {
    suspend operator fun invoke(): Result<List<ReleaseNoteModel>> = releaseNotesRepository
        .getReleaseNotes()
        .map { notes ->
            notes.sortedWith(VersionComparator.reversed())
        }
}
