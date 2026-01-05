package com.quare.bibleplanner.feature.books.presentation.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.Acts
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.GeneralEpistles
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.Gospels
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.HistoricalBooks
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.MajorProphets
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.MinorProphets
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.PaulineEpistles
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.Pentateuch
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.Revelation
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup.WisdomBooks

class BookGroupMapper {
    fun fromBookId(bookId: BookId): BookGroup = when (bookId) {
        BookId.GEN,
        BookId.EXO,
        BookId.LEV,
        BookId.NUM,
        BookId.DEU,
        -> Pentateuch

        BookId.JOS,
        BookId.JDG,
        BookId.RUT,
        BookId.FIRST_SA,
        BookId.SECOND_SA,
        BookId.FIRST_KI,
        BookId.SECOND_KI,
        BookId.FIRST_CH,
        BookId.SECOND_CH,
        BookId.EZR,
        BookId.NEH,
        BookId.EST,
        -> HistoricalBooks

        BookId.JOB,
        BookId.PSA,
        BookId.PRO,
        BookId.ECC,
        BookId.SNG,
        -> WisdomBooks

        BookId.ISA,
        BookId.JER,
        BookId.LAM,
        BookId.EZK,
        BookId.DAN,
        -> MajorProphets

        BookId.HOS,
        BookId.JOL,
        BookId.AMO,
        BookId.OBA,
        BookId.JON,
        BookId.MIC,
        BookId.NAM,
        BookId.HAB,
        BookId.ZEP,
        BookId.HAG,
        BookId.ZEC,
        BookId.MAL,
        -> MinorProphets

        BookId.MAT,
        BookId.MRK,
        BookId.LUK,
        BookId.JHN,
        -> Gospels

        BookId.ACT -> Acts

        BookId.ROM,
        BookId.FIRST_CO,
        BookId.SECOND_CO,
        BookId.GAL,
        BookId.EPH,
        BookId.PHP,
        BookId.COL,
        BookId.FIRST_TH,
        BookId.SECOND_TH,
        BookId.FIRST_TI,
        BookId.SECOND_TI,
        BookId.TIT,
        BookId.PHM,
        -> PaulineEpistles

        BookId.HEB,
        BookId.JAS,
        BookId.FIRST_PE,
        BookId.SECOND_PE,
        BookId.FIRST_JN,
        BookId.SECOND_JN,
        BookId.THIRD_JN,
        BookId.JUD,
        -> GeneralEpistles

        BookId.REV -> Revelation
    }
}
