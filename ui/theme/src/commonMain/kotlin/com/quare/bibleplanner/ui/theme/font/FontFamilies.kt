package com.quare.bibleplanner.ui.theme.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import bibleplanner.ui.theme.generated.resources.Res
import bibleplanner.ui.theme.generated.resources.atkinson_hyperlegible
import bibleplanner.ui.theme.generated.resources.bitter
import bibleplanner.ui.theme.generated.resources.caveat
import bibleplanner.ui.theme.generated.resources.crimson_pro
import bibleplanner.ui.theme.generated.resources.dm_serif_display
import bibleplanner.ui.theme.generated.resources.eb_garamond
import bibleplanner.ui.theme.generated.resources.literata
import bibleplanner.ui.theme.generated.resources.lora
import bibleplanner.ui.theme.generated.resources.montserrat
import bibleplanner.ui.theme.generated.resources.nunito_sans
import bibleplanner.ui.theme.generated.resources.open_dyslexic
import bibleplanner.ui.theme.generated.resources.playfair_display
import org.jetbrains.compose.resources.Font

/**
 * Each family ships a single Regular file; bold and italic are synthesised, which is all the reader
 * and the share card ask for.
 */
@Composable
fun ReaderFont.toFontFamily(): FontFamily = when (this) {
    ReaderFont.LORA -> FontFamily(Font(Res.font.lora))
    ReaderFont.LITERATA -> FontFamily(Font(Res.font.literata))
    ReaderFont.GARAMOND -> FontFamily(Font(Res.font.eb_garamond))
    ReaderFont.CRIMSON -> FontFamily(Font(Res.font.crimson_pro))
    ReaderFont.BITTER -> FontFamily(Font(Res.font.bitter))
    ReaderFont.SYSTEM -> FontFamily.SansSerif
    ReaderFont.NUNITO -> FontFamily(Font(Res.font.nunito_sans))
    ReaderFont.ATKINSON -> FontFamily(Font(Res.font.atkinson_hyperlegible))
    ReaderFont.DYSLEXIC -> FontFamily(Font(Res.font.open_dyslexic))
}

@Composable
fun ShareCardFont.toFontFamily(): FontFamily = when (this) {
    ShareCardFont.LORA -> FontFamily(Font(Res.font.lora))
    ShareCardFont.PLAYFAIR -> FontFamily(Font(Res.font.playfair_display))
    ShareCardFont.DM_SERIF -> FontFamily(Font(Res.font.dm_serif_display))
    ShareCardFont.GARAMOND -> FontFamily(Font(Res.font.eb_garamond))
    ShareCardFont.CAVEAT -> FontFamily(Font(Res.font.caveat))
    ShareCardFont.MONTSERRAT -> FontFamily(Font(Res.font.montserrat))
}

/** The serif the chapter header and quote blocks are set in, regardless of the reader's choice. */
@Composable
fun displaySerifFontFamily(): FontFamily = FontFamily(Font(Res.font.lora))
