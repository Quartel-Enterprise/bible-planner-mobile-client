package com.quare.bibleplanner.feature.day.domain.model

sealed interface UpdateReadStatusOfPassageStrategy {

    val passageIndex: Int

    data class EntireBook(override val passageIndex: Int) : UpdateReadStatusOfPassageStrategy
    data class Chapter(
        override val passageIndex: Int,
        val chapterIndex: Int
    ) : UpdateReadStatusOfPassageStrategy
}
