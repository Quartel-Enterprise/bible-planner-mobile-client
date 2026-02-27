package com.quare.bibleplanner.feature.read.domain.model

data class ReadNavigationSuggestionsModel(
    val previous: ReadNavigationSuggestionModel?,
    val next: ReadNavigationSuggestionModel?,
)
