package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.readingplan.domain.model.ResumeReadTarget

internal fun interface ResolveResumeReadTarget {
    suspend operator fun invoke(passages: List<PassageModel>): Result<ResumeReadTarget?>
}
