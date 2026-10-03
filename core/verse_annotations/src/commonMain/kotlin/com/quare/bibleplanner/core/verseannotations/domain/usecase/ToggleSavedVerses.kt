package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

fun interface ToggleSavedVerses {
    suspend operator fun invoke(refs: List<VerseRef>): Boolean
}
