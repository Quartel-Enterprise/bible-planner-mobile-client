package com.quare.bibleplanner.feature.read.screenshots

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.ui.theme.font.ReaderFont

/**
 * Genesis 1:1-12, the opening of the chapter the day and study screenshots also land on, in the
 * three listing locales, along with the version each locale's reader chip names.
 *
 * The app downloads its Bible versions, so no verse text ships in this repo: these verses are
 * transcribed from public-domain translations (World English Bible, Almeida 1911, Reina-Valera
 * 1909) rather than from the app's own data, which the team accepted for the store listing. The
 * chip still names the version the app ships — ACF for Portuguese — so the wording here is not the
 * wording a Portuguese user reads; only the layout is representative.
 */
private const val CHAPTER = 1
private val versionByLocale = mapOf(
    "en-US" to "WEB",
    "pt-BR" to "ACF",
    "es" to "RVR",
)
private val versesByLocale = mapOf(
    "en-US" to listOf(
        "In the beginning, God created the heavens and the earth.",
        "The earth was formless and empty. Darkness was on the surface of the deep and God's " +
            "Spirit was hovering over the surface of the waters.",
        "God said, \"Let there be light,\" and there was light.",
        "God saw the light, and saw that it was good. God divided the light from the darkness.",
        "God called the light \"day\", and the darkness he called \"night\". There was evening and " +
            "there was morning, the first day.",
        "God said, \"Let there be an expanse in the middle of the waters, and let it divide the " +
            "waters from the waters.\"",
        "God made the expanse, and divided the waters which were under the expanse from the waters " +
            "which were above the expanse; and it was so.",
        "God called the expanse \"sky\". There was evening and there was morning, a second day.",
        "God said, \"Let the waters under the sky be gathered together to one place, and let the " +
            "dry land appear;\" and it was so.",
        "God called the dry land \"earth\", and the gathering together of the waters he called " +
            "\"seas\". God saw that it was good.",
        "God said, \"Let the earth yield grass, herbs yielding seeds, and fruit trees bearing " +
            "fruit after their kind, with their seeds in it, on the earth;\" and it was so.",
        "The earth yielded grass, herbs yielding seed after their kind, and trees bearing fruit, " +
            "with their seeds in it, after their kind; and God saw that it was good.",
    ),
    "pt-BR" to listOf(
        "No princípio criou Deus os céus e a terra.",
        "E a terra era sem forma e vazia; e havia trevas sobre a face do abismo; e o Espírito de " +
            "Deus se movia sobre a face das águas.",
        "E disse Deus: Haja luz; e houve luz.",
        "E viu Deus que era boa a luz; e fez Deus separação entre a luz e as trevas.",
        "E Deus chamou à luz Dia; e às trevas chamou Noite. E foi a tarde e a manhã, o dia primeiro.",
        "E disse Deus: Haja uma expansão no meio das águas, e haja separação entre águas e águas.",
        "E fez Deus a expansão, e fez separação entre as águas que estavam debaixo da expansão e " +
            "as águas que estavam sobre a expansão; e assim foi.",
        "E chamou Deus à expansão Céus; e foi a tarde e a manhã, o dia segundo.",
        "E disse Deus: Ajuntem-se as águas debaixo dos céus num lugar; e apareça a porção seca; e " +
            "assim foi.",
        "E chamou Deus à porção seca Terra; e ao ajuntamento das águas chamou Mares; e viu Deus " +
            "que era bom.",
        "E disse Deus: Produza a terra erva verde, erva que dê semente, árvore frutífera que dê " +
            "fruto segundo a sua espécie, cuja semente esteja nela sobre a terra; e assim foi.",
        "E a terra produziu erva, erva dando semente conforme a sua espécie, e a árvore frutífera, " +
            "cuja semente está nela conforme a sua espécie; e viu Deus que era bom.",
    ),
    "es" to listOf(
        "En el principio creó Dios los cielos y la tierra.",
        "Y la tierra estaba desordenada y vacía, y las tinieblas estaban sobre la faz del abismo, " +
            "y el Espíritu de Dios se movía sobre la faz de las aguas.",
        "Y dijo Dios: Sea la luz; y fue la luz.",
        "Y vio Dios que la luz era buena; y apartó Dios la luz de las tinieblas.",
        "Y llamó Dios a la luz Día, y a las tinieblas llamó Noche; y fue la tarde y la mañana un día.",
        "Y dijo Dios: Haya expansión en medio de las aguas, y separe las aguas de las aguas.",
        "E hizo Dios la expansión, y apartó las aguas que estaban debajo de la expansión, de las " +
            "aguas que estaban sobre la expansión; y fue así.",
        "Y llamó Dios a la expansión Cielos; y fue la tarde y la mañana el día segundo.",
        "Y dijo Dios: Júntense las aguas que están debajo de los cielos en un lugar, y descúbrase " +
            "lo seco; y fue así.",
        "Y llamó Dios a lo seco Tierra, y a la reunión de las aguas llamó Mares; y vio Dios que " +
            "era bueno.",
        "Y dijo Dios: Produzca la tierra hierba verde, hierba que dé simiente; árbol de fruto que " +
            "dé fruto según su género, que su simiente esté en él, sobre la tierra; y fue así.",
        "Y produjo la tierra hierba verde, hierba que da simiente según su naturaleza, y árbol que " +
            "da fruto, cuya simiente está en él, según su género; y vio Dios que era bueno.",
    ),
)

internal fun readUiState(locale: String): ReadUiState {
    val bookStringResource = BookId.GEN.toBookNameResource()
    return ReadUiState(
        header = ReadHeaderUiModel(
            bookId = BookId.GEN,
            bookStringResource = bookStringResource,
            chapterNumber = CHAPTER,
            isChapterRead = false,
            navigationSuggestions = ReadNavigationSuggestionsModel(
                // Genesis 1 is where the Bible starts, so there is nothing to go back to.
                previous = null,
                next = ReadNavigationSuggestionModel(
                    bookId = BookId.GEN,
                    chapterNumber = CHAPTER + 1,
                ),
            ),
            versionAbbreviation = Loadable.Loaded(versionByLocale.getValue(locale)),
        ),
        content = ReadContentUiState.Success(
            chapters = listOf(
                ReadChapterUiModel(
                    chapter = ChapterRef(
                        bibleVersionId = versionByLocale.getValue(locale),
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                    ),
                    bookStringResource = bookStringResource,
                    isRead = false,
                    verses = versesByLocale.getValue(locale).mapIndexed { index, text ->
                        VerseUiModel(
                            number = index + 1,
                            heading = null,
                            text = text,
                            isSelected = false,
                            highlightColor = null,
                            isSaved = false,
                            noteId = null,
                        )
                    },
                ),
            ),
        ),
        settings = ReaderSettingsModel(
            fontSizeSp = ReaderFontSize.DEFAULT,
            font = ReaderFont.LORA,
            isRulerEnabled = false,
            rulerLines = ReaderRulerLines.DEFAULT,
            isFocusedVerseEnabled = false,
            isVerticalReadingEnabled = false,
        ),
        isLoadingPreviousChapter = false,
        isLoadingNextChapter = false,
        dayCompletionBanner = null,
    )
}
