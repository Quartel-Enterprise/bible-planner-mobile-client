package com.quare.bibleplanner.core.books.presentation.model

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.new_testament
import bibleplanner.core.books.generated.resources.old_testament
import org.jetbrains.compose.resources.StringResource

enum class BookTestament(
    val titleRes: StringResource,
) {
    OldTestament(Res.string.old_testament),
    NewTestament(Res.string.new_testament),
}
