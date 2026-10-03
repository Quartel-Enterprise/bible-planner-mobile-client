package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

fun interface ToggleSavedVerses {
    // Why: returns true when the verses ended up saved, false when they were unsaved.
    suspend operator fun invoke(refs: List<VerseRef>): Boolean
}
