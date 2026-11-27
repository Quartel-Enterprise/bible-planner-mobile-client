package com.quare.bibleplanner.core.books.presentation.model

sealed interface BookGroup {
    val testament: BookTestament

    data object Pentateuch : BookGroup {
        override val testament = BookTestament.OldTestament
    }

    data object HistoricalBooks : BookGroup {
        override val testament = BookTestament.OldTestament
    }

    data object WisdomBooks : BookGroup {
        override val testament = BookTestament.OldTestament
    }

    data object MajorProphets : BookGroup {
        override val testament = BookTestament.OldTestament
    }

    data object MinorProphets : BookGroup {
        override val testament = BookTestament.OldTestament
    }

    data object Gospels : BookGroup {
        override val testament = BookTestament.NewTestament
    }

    data object Acts : BookGroup {
        override val testament = BookTestament.NewTestament
    }

    data object PaulineEpistles : BookGroup {
        override val testament = BookTestament.NewTestament
    }

    data object GeneralEpistles : BookGroup {
        override val testament = BookTestament.NewTestament
    }

    data object Revelation : BookGroup {
        override val testament = BookTestament.NewTestament
    }
}
