package com.quare.bibleplanner.feature.books.presentation.binding

sealed interface BookTestament {
    data object OldTestament : BookTestament

    data object NewTestament : BookTestament
}
