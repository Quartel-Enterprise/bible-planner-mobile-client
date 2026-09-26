package com.quare.bibleplanner.e2e.harness

internal object FakeBibles {
    val englishDefault = FakeBibleVersion(
        id = "WEB",
        name = "World English Bible",
        language = "en",
        country = "US",
    )
    val englishAlternative = FakeBibleVersion(
        id = "KJV",
        name = "King James Version",
        language = "en",
        country = "US",
    )
    val versions: List<FakeBibleVersion> = listOf(englishDefault, englishAlternative)

    fun verseText(
        version: FakeBibleVersion,
        bookDirectory: String,
        chapter: Int,
        verse: Int,
    ): String = "${version.id} $bookDirectory $chapter:$verse"
}
