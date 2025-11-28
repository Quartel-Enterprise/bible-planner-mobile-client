package com.quare.bibleplanner.core.books.data.provider

import com.quare.bibleplanner.core.model.book.BookId

class BookMapsProvider {
    private val pentateuch = mapOf(
        "GEN" to BookId.GEN,
        "EXO" to BookId.EXO,
        "LEV" to BookId.LEV,
        "NUM" to BookId.NUM,
        "DEU" to BookId.DEU,
    )

    private val historicalBooks = mapOf(
        "JOS" to BookId.JOS,
        "JDG" to BookId.JDG,
        "RUT" to BookId.RUT,
        "1SA" to BookId.FIRST_SA,
        "2SA" to BookId.SECOND_SA,
        "1KI" to BookId.FIRST_KI,
        "2KI" to BookId.SECOND_KI,
        "1CH" to BookId.FIRST_CH,
        "2CH" to BookId.SECOND_CH,
        "EZR" to BookId.EZR,
        "NEH" to BookId.NEH,
        "EST" to BookId.EST,
    )

    private val wisdomBooks = mapOf(
        "JOB" to BookId.JOB,
        "PSA" to BookId.PSA,
        "PRO" to BookId.PRO,
        "ECC" to BookId.ECC,
        "SNG" to BookId.SNG,
    )

    private val majorProphets = mapOf(
        "ISA" to BookId.ISA,
        "JER" to BookId.JER,
        "LAM" to BookId.LAM,
        "EZK" to BookId.EZK,
        "DAN" to BookId.DAN,
    )

    private val minorProphets = mapOf(
        "HOS" to BookId.HOS,
        "JOL" to BookId.JOL,
        "AMO" to BookId.AMO,
        "OBA" to BookId.OBA,
        "JON" to BookId.JON,
        "MIC" to BookId.MIC,
        "NAM" to BookId.NAM,
        "HAB" to BookId.HAB,
        "ZEP" to BookId.ZEP,
        "HAG" to BookId.HAG,
        "ZEC" to BookId.ZEC,
        "MAL" to BookId.MAL,
    )

    private val gospels = mapOf(
        "MAT" to BookId.MAT,
        "MRK" to BookId.MRK,
        "LUK" to BookId.LUK,
        "JHN" to BookId.JHN,
    )

    private val acts = mapOf(
        "ACT" to BookId.ACT,
    )

    private val paulineEpistles = mapOf(
        "ROM" to BookId.ROM,
        "1CO" to BookId.FIRST_CO,
        "2CO" to BookId.SECOND_CO,
        "GAL" to BookId.GAL,
        "EPH" to BookId.EPH,
        "PHP" to BookId.PHP,
        "COL" to BookId.COL,
        "1TH" to BookId.FIRST_TH,
        "2TH" to BookId.SECOND_TH,
        "1TI" to BookId.FIRST_TI,
        "2TI" to BookId.SECOND_TI,
        "TIT" to BookId.TIT,
        "PHM" to BookId.PHM,
    )

    private val generalEpistles = mapOf(
        "HEB" to BookId.HEB,
        "JAS" to BookId.JAS,
        "1PE" to BookId.FIRST_PE,
        "2PE" to BookId.SECOND_PE,
        "1JN" to BookId.FIRST_JN,
        "2JN" to BookId.SECOND_JN,
        "3JN" to BookId.THIRD_JN,
        "JUD" to BookId.JUD,
    )

    private val revelation = mapOf(
        "REV" to BookId.REV,
    )

    private val oldTestament = listOf(
        pentateuch,
        historicalBooks,
        wisdomBooks,
        majorProphets,
        minorProphets,
    )

    private val newTestament = listOf(
        gospels,
        acts,
        paulineEpistles,
        generalEpistles,
        revelation,
    )

    val bookMaps: List<Map<String, BookId>> = oldTestament + newTestament
}
