package com.quare.bibleplanner.feature.read.presentation.appearance

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_font_atkinson
import bibleplanner.feature.read.generated.resources.reader_font_bitter
import bibleplanner.feature.read.generated.resources.reader_font_crimson
import bibleplanner.feature.read.generated.resources.reader_font_dyslexic
import bibleplanner.feature.read.generated.resources.reader_font_garamond
import bibleplanner.feature.read.generated.resources.reader_font_literata
import bibleplanner.feature.read.generated.resources.reader_font_lora
import bibleplanner.feature.read.generated.resources.reader_font_nunito
import bibleplanner.feature.read.generated.resources.reader_font_system
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import org.jetbrains.compose.resources.StringResource

internal val ReaderFont.labelResource: StringResource
    get() = when (this) {
        ReaderFont.LORA -> Res.string.reader_font_lora
        ReaderFont.LITERATA -> Res.string.reader_font_literata
        ReaderFont.GARAMOND -> Res.string.reader_font_garamond
        ReaderFont.CRIMSON -> Res.string.reader_font_crimson
        ReaderFont.BITTER -> Res.string.reader_font_bitter
        ReaderFont.SYSTEM -> Res.string.reader_font_system
        ReaderFont.NUNITO -> Res.string.reader_font_nunito
        ReaderFont.ATKINSON -> Res.string.reader_font_atkinson
        ReaderFont.DYSLEXIC -> Res.string.reader_font_dyslexic
    }
