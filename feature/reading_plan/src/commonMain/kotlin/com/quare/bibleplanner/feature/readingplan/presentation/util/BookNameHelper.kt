package com.quare.bibleplanner.feature.readingplan.presentation.util

import androidx.compose.runtime.Composable
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.book_act
import bibleplanner.feature.reading_plan.generated.resources.book_amo
import bibleplanner.feature.reading_plan.generated.resources.book_col
import bibleplanner.feature.reading_plan.generated.resources.book_dan
import bibleplanner.feature.reading_plan.generated.resources.book_deu
import bibleplanner.feature.reading_plan.generated.resources.book_ecc
import bibleplanner.feature.reading_plan.generated.resources.book_eph
import bibleplanner.feature.reading_plan.generated.resources.book_est
import bibleplanner.feature.reading_plan.generated.resources.book_exo
import bibleplanner.feature.reading_plan.generated.resources.book_ezk
import bibleplanner.feature.reading_plan.generated.resources.book_ezr
import bibleplanner.feature.reading_plan.generated.resources.book_first_ch
import bibleplanner.feature.reading_plan.generated.resources.book_first_co
import bibleplanner.feature.reading_plan.generated.resources.book_first_jn
import bibleplanner.feature.reading_plan.generated.resources.book_first_ki
import bibleplanner.feature.reading_plan.generated.resources.book_first_pe
import bibleplanner.feature.reading_plan.generated.resources.book_first_sa
import bibleplanner.feature.reading_plan.generated.resources.book_first_th
import bibleplanner.feature.reading_plan.generated.resources.book_first_ti
import bibleplanner.feature.reading_plan.generated.resources.book_gal
import bibleplanner.feature.reading_plan.generated.resources.book_gen
import bibleplanner.feature.reading_plan.generated.resources.book_hab
import bibleplanner.feature.reading_plan.generated.resources.book_hag
import bibleplanner.feature.reading_plan.generated.resources.book_heb
import bibleplanner.feature.reading_plan.generated.resources.book_hos
import bibleplanner.feature.reading_plan.generated.resources.book_isa
import bibleplanner.feature.reading_plan.generated.resources.book_jas
import bibleplanner.feature.reading_plan.generated.resources.book_jdg
import bibleplanner.feature.reading_plan.generated.resources.book_jer
import bibleplanner.feature.reading_plan.generated.resources.book_jhn
import bibleplanner.feature.reading_plan.generated.resources.book_job
import bibleplanner.feature.reading_plan.generated.resources.book_jol
import bibleplanner.feature.reading_plan.generated.resources.book_jon
import bibleplanner.feature.reading_plan.generated.resources.book_jos
import bibleplanner.feature.reading_plan.generated.resources.book_jud
import bibleplanner.feature.reading_plan.generated.resources.book_lam
import bibleplanner.feature.reading_plan.generated.resources.book_lev
import bibleplanner.feature.reading_plan.generated.resources.book_luk
import bibleplanner.feature.reading_plan.generated.resources.book_mal
import bibleplanner.feature.reading_plan.generated.resources.book_mat
import bibleplanner.feature.reading_plan.generated.resources.book_mic
import bibleplanner.feature.reading_plan.generated.resources.book_mrk
import bibleplanner.feature.reading_plan.generated.resources.book_nam
import bibleplanner.feature.reading_plan.generated.resources.book_neh
import bibleplanner.feature.reading_plan.generated.resources.book_num
import bibleplanner.feature.reading_plan.generated.resources.book_oba
import bibleplanner.feature.reading_plan.generated.resources.book_phm
import bibleplanner.feature.reading_plan.generated.resources.book_php
import bibleplanner.feature.reading_plan.generated.resources.book_pro
import bibleplanner.feature.reading_plan.generated.resources.book_psa
import bibleplanner.feature.reading_plan.generated.resources.book_rev
import bibleplanner.feature.reading_plan.generated.resources.book_rom
import bibleplanner.feature.reading_plan.generated.resources.book_rut
import bibleplanner.feature.reading_plan.generated.resources.book_second_ch
import bibleplanner.feature.reading_plan.generated.resources.book_second_co
import bibleplanner.feature.reading_plan.generated.resources.book_second_jn
import bibleplanner.feature.reading_plan.generated.resources.book_second_ki
import bibleplanner.feature.reading_plan.generated.resources.book_second_pe
import bibleplanner.feature.reading_plan.generated.resources.book_second_sa
import bibleplanner.feature.reading_plan.generated.resources.book_second_th
import bibleplanner.feature.reading_plan.generated.resources.book_second_ti
import bibleplanner.feature.reading_plan.generated.resources.book_sng
import bibleplanner.feature.reading_plan.generated.resources.book_third_jn
import bibleplanner.feature.reading_plan.generated.resources.book_tit
import bibleplanner.feature.reading_plan.generated.resources.book_zec
import bibleplanner.feature.reading_plan.generated.resources.book_zep
import com.quare.bibleplanner.core.model.book.BookId
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Maps BookId to the corresponding string resource
 */
@Composable
internal fun BookId.toBookNameResource(): StringResource = when (this) {
    BookId.GEN -> Res.string.book_gen
    BookId.EXO -> Res.string.book_exo
    BookId.LEV -> Res.string.book_lev
    BookId.NUM -> Res.string.book_num
    BookId.DEU -> Res.string.book_deu
    BookId.JOS -> Res.string.book_jos
    BookId.JDG -> Res.string.book_jdg
    BookId.RUT -> Res.string.book_rut
    BookId.FIRST_SA -> Res.string.book_first_sa
    BookId.SECOND_SA -> Res.string.book_second_sa
    BookId.FIRST_KI -> Res.string.book_first_ki
    BookId.SECOND_KI -> Res.string.book_second_ki
    BookId.FIRST_CH -> Res.string.book_first_ch
    BookId.SECOND_CH -> Res.string.book_second_ch
    BookId.EZR -> Res.string.book_ezr
    BookId.NEH -> Res.string.book_neh
    BookId.EST -> Res.string.book_est
    BookId.JOB -> Res.string.book_job
    BookId.PSA -> Res.string.book_psa
    BookId.PRO -> Res.string.book_pro
    BookId.ECC -> Res.string.book_ecc
    BookId.SNG -> Res.string.book_sng
    BookId.ISA -> Res.string.book_isa
    BookId.JER -> Res.string.book_jer
    BookId.LAM -> Res.string.book_lam
    BookId.EZK -> Res.string.book_ezk
    BookId.DAN -> Res.string.book_dan
    BookId.HOS -> Res.string.book_hos
    BookId.JOL -> Res.string.book_jol
    BookId.AMO -> Res.string.book_amo
    BookId.OBA -> Res.string.book_oba
    BookId.JON -> Res.string.book_jon
    BookId.MIC -> Res.string.book_mic
    BookId.NAM -> Res.string.book_nam
    BookId.HAB -> Res.string.book_hab
    BookId.ZEP -> Res.string.book_zep
    BookId.HAG -> Res.string.book_hag
    BookId.ZEC -> Res.string.book_zec
    BookId.MAL -> Res.string.book_mal
    BookId.MAT -> Res.string.book_mat
    BookId.MRK -> Res.string.book_mrk
    BookId.LUK -> Res.string.book_luk
    BookId.JHN -> Res.string.book_jhn
    BookId.ACT -> Res.string.book_act
    BookId.ROM -> Res.string.book_rom
    BookId.FIRST_CO -> Res.string.book_first_co
    BookId.SECOND_CO -> Res.string.book_second_co
    BookId.GAL -> Res.string.book_gal
    BookId.EPH -> Res.string.book_eph
    BookId.PHP -> Res.string.book_php
    BookId.COL -> Res.string.book_col
    BookId.FIRST_TH -> Res.string.book_first_th
    BookId.SECOND_TH -> Res.string.book_second_th
    BookId.FIRST_TI -> Res.string.book_first_ti
    BookId.SECOND_TI -> Res.string.book_second_ti
    BookId.TIT -> Res.string.book_tit
    BookId.PHM -> Res.string.book_phm
    BookId.HEB -> Res.string.book_heb
    BookId.JAS -> Res.string.book_jas
    BookId.FIRST_PE -> Res.string.book_first_pe
    BookId.SECOND_PE -> Res.string.book_second_pe
    BookId.FIRST_JN -> Res.string.book_first_jn
    BookId.SECOND_JN -> Res.string.book_second_jn
    BookId.THIRD_JN -> Res.string.book_third_jn
    BookId.JUD -> Res.string.book_jud
    BookId.REV -> Res.string.book_rev
}

/**
 * Get book name as a localized string using string resources
 */
@Composable
internal fun BookId.getBookName(): String = stringResource(toBookNameResource())
