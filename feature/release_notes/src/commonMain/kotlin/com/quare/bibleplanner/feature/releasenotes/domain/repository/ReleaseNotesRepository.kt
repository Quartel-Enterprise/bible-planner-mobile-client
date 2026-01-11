package com.quare.bibleplanner.feature.releasenotes.domain.repository

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import kotlinx.datetime.LocalDate

interface ReleaseNotesRepository {
    suspend fun getReleaseNotes(): Result<List<ReleaseNoteModel>>
}
