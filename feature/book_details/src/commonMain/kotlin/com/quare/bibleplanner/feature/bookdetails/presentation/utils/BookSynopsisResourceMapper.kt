package com.quare.bibleplanner.feature.bookdetails.presentation.utils

import bibleplanner.feature.book_details.generated.resources.*
import bibleplanner.feature.book_details.generated.resources.Res
import com.quare.bibleplanner.core.model.book.BookId
import org.jetbrains.compose.resources.StringResource

fun BookId.toSynopsisResource(): StringResource = when (this) {
    BookId.GEN -> Res.string.bible_book_genesis_synopsis
    BookId.EXO -> Res.string.bible_book_exodus_synopsis
    BookId.LEV -> Res.string.bible_book_leviticus_synopsis
    BookId.NUM -> Res.string.bible_book_numbers_synopsis
    BookId.DEU -> Res.string.bible_book_deuteronomy_synopsis
    BookId.JOS -> Res.string.bible_book_joshua_synopsis
    BookId.JDG -> Res.string.bible_book_judges_synopsis
    BookId.RUT -> Res.string.bible_book_ruth_synopsis
    BookId.FIRST_SA -> Res.string.bible_book_1_samuel_synopsis
    BookId.SECOND_SA -> Res.string.bible_book_2_samuel_synopsis
    BookId.FIRST_KI -> Res.string.bible_book_1_kings_synopsis
    BookId.SECOND_KI -> Res.string.bible_book_2_kings_synopsis
    BookId.FIRST_CH -> Res.string.bible_book_1_chronicles_synopsis
    BookId.SECOND_CH -> Res.string.bible_book_2_chronicles_synopsis
    BookId.EZR -> Res.string.bible_book_ezra_synopsis
    BookId.NEH -> Res.string.bible_book_nehemiah_synopsis
    BookId.EST -> Res.string.bible_book_esther_synopsis
    BookId.JOB -> Res.string.bible_book_job_synopsis
    BookId.PSA -> Res.string.bible_book_psalms_synopsis
    BookId.PRO -> Res.string.bible_book_proverbs_synopsis
    BookId.ECC -> Res.string.bible_book_ecclesiastes_synopsis
    BookId.SNG -> Res.string.bible_book_song_of_songs_synopsis
    BookId.ISA -> Res.string.bible_book_isaiah_synopsis
    BookId.JER -> Res.string.bible_book_jeremiah_synopsis
    BookId.LAM -> Res.string.bible_book_lamentations_synopsis
    BookId.EZK -> Res.string.bible_book_ezekiel_synopsis
    BookId.DAN -> Res.string.bible_book_daniel_synopsis
    BookId.HOS -> Res.string.bible_book_hosea_synopsis
    BookId.JOL -> Res.string.bible_book_joel_synopsis
    BookId.AMO -> Res.string.bible_book_amos_synopsis
    BookId.OBA -> Res.string.bible_book_obadiah_synopsis
    BookId.JON -> Res.string.bible_book_jonah_synopsis
    BookId.MIC -> Res.string.bible_book_micah_synopsis
    BookId.NAM -> Res.string.bible_book_nahum_synopsis
    BookId.HAB -> Res.string.bible_book_habakkuk_synopsis
    BookId.ZEP -> Res.string.bible_book_zephaniah_synopsis
    BookId.HAG -> Res.string.bible_book_haggai_synopsis
    BookId.ZEC -> Res.string.bible_book_zechariah_synopsis
    BookId.MAL -> Res.string.bible_book_malachi_synopsis
    BookId.MAT -> Res.string.bible_book_matthew_synopsis
    BookId.MRK -> Res.string.bible_book_mark_synopsis
    BookId.LUK -> Res.string.bible_book_luke_synopsis
    BookId.JHN -> Res.string.bible_book_john_synopsis
    BookId.ACT -> Res.string.bible_book_acts_synopsis
    BookId.ROM -> Res.string.bible_book_romans_synopsis
    BookId.FIRST_CO -> Res.string.bible_book_1_corinthians_synopsis
    BookId.SECOND_CO -> Res.string.bible_book_2_corinthians_synopsis
    BookId.GAL -> Res.string.bible_book_galatians_synopsis
    BookId.EPH -> Res.string.bible_book_ephesians_synopsis
    BookId.PHP -> Res.string.bible_book_philippians_synopsis
    BookId.COL -> Res.string.bible_book_colossians_synopsis
    BookId.FIRST_TH -> Res.string.bible_book_1_thessalonians_synopsis
    BookId.SECOND_TH -> Res.string.bible_book_2_thessalonians_synopsis
    BookId.FIRST_TI -> Res.string.bible_book_1_timothy_synopsis
    BookId.SECOND_TI -> Res.string.bible_book_2_timothy_synopsis
    BookId.TIT -> Res.string.bible_book_titus_synopsis
    BookId.PHM -> Res.string.bible_book_philemon_synopsis
    BookId.HEB -> Res.string.bible_book_hebrews_synopsis
    BookId.JAS -> Res.string.bible_book_james_synopsis
    BookId.FIRST_PE -> Res.string.bible_book_1_peter_synopsis
    BookId.SECOND_PE -> Res.string.bible_book_2_peter_synopsis
    BookId.FIRST_JN -> Res.string.bible_book_1_john_synopsis
    BookId.SECOND_JN -> Res.string.bible_book_2_john_synopsis
    BookId.THIRD_JN -> Res.string.bible_book_3_john_synopsis
    BookId.JUD -> Res.string.bible_book_jude_synopsis
    BookId.REV -> Res.string.bible_book_revelation_synopsis
}
