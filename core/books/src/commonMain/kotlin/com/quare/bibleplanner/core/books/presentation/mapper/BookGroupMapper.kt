package com.quare.bibleplanner.core.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.Acts
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.GeneralEpistles
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.Gospels
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.HistoricalBooks
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.MajorProphets
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.MinorProphets
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.PaulineEpistles
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.Pentateuch
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.Revelation
import com.quare.bibleplanner.core.books.presentation.model.BookGroup.WisdomBooks
import com.quare.bibleplanner.core.model.book.BookId

class BookGroupMapper {
    fun fromBookId(bookId: BookId): BookGroup = when (bookId) {
        // Pentateuch
        BookId.GEN, // Genesis
        BookId.EXO, // Exodus
        BookId.LEV, // Leviticus
        BookId.NUM, // Numbers
        BookId.DEU, // Deuteronomy
        -> Pentateuch

        // Historical Books
        BookId.JOS, // Joshua
        BookId.JDG, // Judges
        BookId.RUT, // Ruth
        BookId.FIRST_SA, // 1 Samuel
        BookId.SECOND_SA, // 2 Samuel
        BookId.FIRST_KI, // 1 Kings
        BookId.SECOND_KI, // 2 Kings
        BookId.FIRST_CH, // 1 Chronicles
        BookId.SECOND_CH, // 2 Chronicles
        BookId.EZR, // Ezra
        BookId.NEH, // Nehemiah
        BookId.EST, // Esther
        -> HistoricalBooks

        // Wisdom Books
        BookId.JOB, // Job
        BookId.PSA, // Psalms
        BookId.PRO, // Proverbs
        BookId.ECC, // Ecclesiastes
        BookId.SNG, // Song of Songs
        -> WisdomBooks

        // Major Prophets
        BookId.ISA, // Isaiah
        BookId.JER, // Jeremiah
        BookId.LAM, // Lamentations
        BookId.EZK, // Ezekiel
        BookId.DAN, // Daniel
        -> MajorProphets

        // Minor Prophets
        BookId.HOS, // Hosea
        BookId.JOL, // Joel
        BookId.AMO, // Amos
        BookId.OBA, // Obadiah
        BookId.JON, // Jonah
        BookId.MIC, // Micah
        BookId.NAM, // Nahum
        BookId.HAB, // Habakkuk
        BookId.ZEP, // Zephaniah
        BookId.HAG, // Haggai
        BookId.ZEC, // Zechariah
        BookId.MAL, // Malachi
        -> MinorProphets

        // Gospels
        BookId.MAT, // Matthew
        BookId.MRK, // Mark
        BookId.LUK, // Luke
        BookId.JHN, // John
        -> Gospels

        // Acts
        BookId.ACT, // Acts of the Apostles
        -> Acts

        // Pauline Epistles
        BookId.ROM, // Romans
        BookId.FIRST_CO, // 1 Corinthians
        BookId.SECOND_CO, // 2 Corinthians
        BookId.GAL, // Galatians
        BookId.EPH, // Ephesians
        BookId.PHP, // Philippians
        BookId.COL, // Colossians
        BookId.FIRST_TH, // 1 Thessalonians
        BookId.SECOND_TH, // 2 Thessalonians
        BookId.FIRST_TI, // 1 Timothy
        BookId.SECOND_TI, // 2 Timothy
        BookId.TIT, // Titus
        BookId.PHM, // Philemon
        -> PaulineEpistles

        // General Epistles
        BookId.HEB, // Hebrews
        BookId.JAS, // James
        BookId.FIRST_PE, // 1 Peter
        BookId.SECOND_PE, // 2 Peter
        BookId.FIRST_JN, // 1 John
        BookId.SECOND_JN, // 2 John
        BookId.THIRD_JN, // 3 John
        BookId.JUD, // Jude
        -> GeneralEpistles

        // Revelation
        BookId.REV, // Revelation
        -> Revelation
    }
}
