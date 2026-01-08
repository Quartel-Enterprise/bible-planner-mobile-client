package com.quare.bibleplanner.feature.books.presentation.binding

import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.new_testament
import bibleplanner.feature.books.generated.resources.old_testament
import org.jetbrains.compose.resources.StringResource

enum class BookTestament(
    val titleRes: StringResource,
) {
    OldTestament(Res.string.old_testament),
    NewTestament(Res.string.new_testament),
}
