package com.quare.bibleplanner.feature.releasenotes.domain.utils

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.core.utils.version.VersionComparator as SemverComparator

object VersionComparator : Comparator<ReleaseNoteModel> {
    override fun compare(
        a: ReleaseNoteModel,
        b: ReleaseNoteModel,
    ): Int = SemverComparator.compare(a.version, b.version)
}
