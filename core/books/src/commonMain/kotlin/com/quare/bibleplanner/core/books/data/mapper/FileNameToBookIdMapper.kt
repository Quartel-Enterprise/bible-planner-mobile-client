package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.model.book.BookId

class FileNameToBookIdMapper {

    fun map(fileName: String): BookId? = when (fileName) {
        // Pentateuch
        "GEN" -> BookId.GEN
        "EXO" -> BookId.EXO
        "LEV" -> BookId.LEV
        "NUM" -> BookId.NUM
        "DEU" -> BookId.DEU

        // Historical Books
        "JOS" -> BookId.JOS
        "JDG" -> BookId.JDG
        "RUT" -> BookId.RUT
        "1SA" -> BookId.FIRST_SA
        "2SA" -> BookId.SECOND_SA
        "1KI" -> BookId.FIRST_KI
        "2KI" -> BookId.SECOND_KI
        "1CH" -> BookId.FIRST_CH
        "2CH" -> BookId.SECOND_CH
        "EZR" -> BookId.EZR
        "NEH" -> BookId.NEH
        "EST" -> BookId.EST

        // Wisdom Books
        "JOB" -> BookId.JOB
        "PSA" -> BookId.PSA
        "PRO" -> BookId.PRO
        "ECC" -> BookId.ECC
        "SNG" -> BookId.SNG

        // Major Prophets
        "ISA" -> BookId.ISA
        "JER" -> BookId.JER
        "LAM" -> BookId.LAM
        "EZK" -> BookId.EZK
        "DAN" -> BookId.DAN

        // Minor Prophets
        "HOS" -> BookId.HOS
        "JOL" -> BookId.JOL
        "AMO" -> BookId.AMO
        "OBA" -> BookId.OBA
        "JON" -> BookId.JON
        "MIC" -> BookId.MIC
        "NAM" -> BookId.NAM
        "HAB" -> BookId.HAB
        "ZEP" -> BookId.ZEP
        "HAG" -> BookId.HAG
        "ZEC" -> BookId.ZEC
        "MAL" -> BookId.MAL

        // Gospels
        "MAT" -> BookId.MAT
        "MRK" -> BookId.MRK
        "LUK" -> BookId.LUK
        "JHN" -> BookId.JHN

        // Acts
        "ACT" -> BookId.ACT

        // Pauline Epistles
        "ROM" -> BookId.ROM
        "1CO" -> BookId.FIRST_CO
        "2CO" -> BookId.SECOND_CO
        "GAL" -> BookId.GAL
        "EPH" -> BookId.EPH
        "PHP" -> BookId.PHP
        "COL" -> BookId.COL
        "1TH" -> BookId.FIRST_TH
        "2TH" -> BookId.SECOND_TH
        "1TI" -> BookId.FIRST_TI
        "2TI" -> BookId.SECOND_TI
        "TIT" -> BookId.TIT
        "PHM" -> BookId.PHM

        // General Epistles
        "HEB" -> BookId.HEB
        "JAS" -> BookId.JAS
        "1PE" -> BookId.FIRST_PE
        "2PE" -> BookId.SECOND_PE
        "1JN" -> BookId.FIRST_JN
        "2JN" -> BookId.SECOND_JN
        "3JN" -> BookId.THIRD_JN
        "JUD" -> BookId.JUD

        // Revelation
        "REV" -> BookId.REV

        else -> null
    }
}

