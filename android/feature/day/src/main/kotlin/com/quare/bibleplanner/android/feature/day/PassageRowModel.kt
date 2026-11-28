package com.quare.bibleplanner.android.feature.day

import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy

internal data class PassageRowModel(
    val passage: PassageModel,
    val chapter: ChapterModel?,
    val isRead: Boolean,
    val clickStrategy: ChapterClickStrategy,
    val toggleStrategy: UpdateReadStatusOfPassageStrategy,
)
