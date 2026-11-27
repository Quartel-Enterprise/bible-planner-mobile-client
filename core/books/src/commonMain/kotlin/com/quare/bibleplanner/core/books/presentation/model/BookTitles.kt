package com.quare.bibleplanner.core.books.presentation.model

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.acts
import bibleplanner.core.books.generated.resources.general_epistles
import bibleplanner.core.books.generated.resources.gospels
import bibleplanner.core.books.generated.resources.historical_books
import bibleplanner.core.books.generated.resources.major_prophets
import bibleplanner.core.books.generated.resources.minor_prophets
import bibleplanner.core.books.generated.resources.new_testament
import bibleplanner.core.books.generated.resources.old_testament
import bibleplanner.core.books.generated.resources.pauline_epistles
import bibleplanner.core.books.generated.resources.pentateuch
import bibleplanner.core.books.generated.resources.revelation
import bibleplanner.core.books.generated.resources.wisdom_books
import org.jetbrains.compose.resources.StringResource

// Why: the models stay plain Kotlin for SwiftUI, which maps them to its own text; Compose reads the titles here.
val BookGroup.titleRes: StringResource
    get() = when (this) {
        BookGroup.Pentateuch -> Res.string.pentateuch
        BookGroup.HistoricalBooks -> Res.string.historical_books
        BookGroup.WisdomBooks -> Res.string.wisdom_books
        BookGroup.MajorProphets -> Res.string.major_prophets
        BookGroup.MinorProphets -> Res.string.minor_prophets
        BookGroup.Gospels -> Res.string.gospels
        BookGroup.Acts -> Res.string.acts
        BookGroup.PaulineEpistles -> Res.string.pauline_epistles
        BookGroup.GeneralEpistles -> Res.string.general_epistles
        BookGroup.Revelation -> Res.string.revelation
    }

val BookTestament.titleRes: StringResource
    get() = when (this) {
        BookTestament.OldTestament -> Res.string.old_testament
        BookTestament.NewTestament -> Res.string.new_testament
    }
