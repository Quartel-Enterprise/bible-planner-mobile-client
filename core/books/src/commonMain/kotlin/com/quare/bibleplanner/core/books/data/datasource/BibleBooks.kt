package com.quare.bibleplanner.core.books.data.datasource

/**
 * Contains the list of all Bible book identifiers.
 */
object BibleBooks {
    private val bookIds: List<String> = listOf(
        // Pentateuch
        "GEN",
        "EXO",
        "LEV",
        "NUM",
        "DEU",
        // Historical Books
        "JOS",
        "JDG",
        "RUT",
        "1SA",
        "2SA",
        "1KI",
        "2KI",
        "1CH",
        "2CH",
        "EZR",
        "NEH",
        "EST",
        // Wisdom Books
        "JOB",
        "PSA",
        "PRO",
        "ECC",
        "SNG",
        // Major Prophets
        "ISA",
        "JER",
        "LAM",
        "EZK",
        "DAN",
        // Minor Prophets
        "HOS",
        "JOL",
        "AMO",
        "OBA",
        "JON",
        "MIC",
        "NAM",
        "HAB",
        "ZEP",
        "HAG",
        "ZEC",
        "MAL",
        // Gospels
        "MAT",
        "MRK",
        "LUK",
        "JHN",
        // Acts
        "ACT",
        // Pauline Epistles
        "ROM",
        "1CO",
        "2CO",
        "GAL",
        "EPH",
        "PHP",
        "COL",
        "1TH",
        "2TH",
        "1TI",
        "2TI",
        "TIT",
        "PHM",
        // General Epistles
        "HEB",
        "JAS",
        "1PE",
        "2PE",
        "1JN",
        "2JN",
        "3JN",
        "JUD",
        // Revelation
        "REV",
    )

    val fileNames: List<String> = bookIds.map { "$it.json" }
}
