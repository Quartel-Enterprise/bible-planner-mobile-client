package com.quare.bibleplanner.core.books.presentation.model

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.acts
import bibleplanner.core.books.generated.resources.general_epistles
import bibleplanner.core.books.generated.resources.gospels
import bibleplanner.core.books.generated.resources.historical_books
import bibleplanner.core.books.generated.resources.major_prophets
import bibleplanner.core.books.generated.resources.minor_prophets
import bibleplanner.core.books.generated.resources.pauline_epistles
import bibleplanner.core.books.generated.resources.pentateuch
import bibleplanner.core.books.generated.resources.revelation
import bibleplanner.core.books.generated.resources.wisdom_books
import org.jetbrains.compose.resources.StringResource

sealed interface BookGroup {
    val titleRes: StringResource
    val testament: BookTestament

    data object Pentateuch : BookGroup {
        override val titleRes = Res.string.pentateuch
        override val testament = BookTestament.OldTestament
    }

    data object HistoricalBooks : BookGroup {
        override val titleRes = Res.string.historical_books
        override val testament = BookTestament.OldTestament
    }

    data object WisdomBooks : BookGroup {
        override val titleRes = Res.string.wisdom_books
        override val testament = BookTestament.OldTestament
    }

    data object MajorProphets : BookGroup {
        override val titleRes = Res.string.major_prophets
        override val testament = BookTestament.OldTestament
    }

    data object MinorProphets : BookGroup {
        override val titleRes = Res.string.minor_prophets
        override val testament = BookTestament.OldTestament
    }

    data object Gospels : BookGroup {
        override val titleRes = Res.string.gospels
        override val testament = BookTestament.NewTestament
    }

    data object Acts : BookGroup {
        override val titleRes = Res.string.acts
        override val testament = BookTestament.NewTestament
    }

    data object PaulineEpistles : BookGroup {
        override val titleRes = Res.string.pauline_epistles
        override val testament = BookTestament.NewTestament
    }

    data object GeneralEpistles : BookGroup {
        override val titleRes = Res.string.general_epistles
        override val testament = BookTestament.NewTestament
    }

    data object Revelation : BookGroup {
        override val titleRes = Res.string.revelation
        override val testament = BookTestament.NewTestament
    }
}
