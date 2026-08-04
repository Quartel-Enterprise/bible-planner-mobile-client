package com.quare.bibleplanner.feature.read.screenshots

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel

private const val CHAPTER = 10
private const val LAST_CHAPTER_OF_PREVIOUS_BOOK = 22

/**
 * 1 Kings 10:1-12, the chapter the plan, day and study screenshots all point at.
 *
 * The app downloads its Bible versions, so no verse text ships in this repo and none of this came
 * from the app's own data — it is transcribed from public-domain translations (World English Bible,
 * Almeida 1911, Reina-Valera 1909) and has not been checked word by word. The app's default version
 * for Portuguese is ACF, which is licensed rather than public domain. Capture these verses from a
 * real device running the version you actually ship before any of this reaches a store listing.
 */
private val versesByLocale = mapOf(
    "en-US" to listOf(
        "When the queen of Sheba heard of the fame of Solomon concerning the name of Yahweh, she " +
            "came to test him with hard questions.",
        "She came to Jerusalem with a very great caravan, with camels that bore spices, very much " +
            "gold, and precious stones.",
        "When she had come to Solomon, she talked with him about all that was in her heart.",
        "Solomon answered all her questions. There wasn't anything hidden from the king which he " +
            "didn't tell her.",
        "When the queen of Sheba had seen all the wisdom of Solomon, the house that he had built,",
        "the food of his table, the sitting of his servants, the attendance of his officials, " +
            "their clothing, his cup bearers,",
        "and his ascent by which he went up to Yahweh's house, there was no more spirit in her.",
        "She said to the king, \"It was a true report that I heard in my own land of your acts and " +
            "of your wisdom.\"",
        "However I didn't believe the words until I came and my eyes had seen it. Behold, not even " +
            "half was told me!",
        "Your wisdom and prosperity exceed the fame which I heard.",
        "Happy are your men, happy are these your servants, who stand continually before you, who " +
            "hear your wisdom.",
        "Blessed is Yahweh your God, who delighted in you, to set you on the throne of Israel.",
    ),
    "pt-BR" to listOf(
        "E ouvindo a rainha de Sabá a fama de Salomão, acerca do nome do Senhor, veio prová-lo " +
            "com questões difíceis.",
        "E veio a Jerusalém com uma grande comitiva, com camelos carregados de especiarias, e " +
            "muitíssimo ouro, e pedras preciosas.",
        "E, vindo ela a Salomão, disse-lhe tudo quanto tinha no seu coração.",
        "E Salomão lhe declarou todas as suas palavras; nenhuma coisa se escondeu ao rei que lhe " +
            "não declarasse.",
        "Vendo, pois, a rainha de Sabá toda a sabedoria de Salomão, e a casa que edificara,",
        "e a comida da sua mesa, e o assentar dos seus servos, e o estar dos seus criados, e as " +
            "vestes deles, e os seus copeiros,",
        "e a subida pela qual ele subia à casa do Senhor, ficou como fora de si.",
        "E disse ao rei: Era verdade a palavra que ouvi na minha terra acerca dos teus feitos e " +
            "da tua sabedoria.",
        "E eu não cria naquelas palavras, até que vim e os meus olhos o viram; eis que não me " +
            "disseram metade.",
        "Sobrepujaste em sabedoria e bens a fama que ouvi.",
        "Bem-aventurados os teus homens, bem-aventurados estes teus servos, que estão sempre " +
            "diante de ti, que ouvem a tua sabedoria.",
        "Bendito seja o Senhor teu Deus, que teve agrado em ti, para te pôr no trono de Israel.",
    ),
    "es" to listOf(
        "Y oyendo la reina de Sabá la fama de Salomón en el nombre de Jehová, vino a probarle con " +
            "preguntas difíciles.",
        "Y vino a Jerusalén con un séquito muy grande, con camellos cargados de especias, y oro " +
            "en gran abundancia, y piedras preciosas.",
        "Y como vino a Salomón, le expuso todo lo que en su corazón tenía.",
        "Y Salomón le declaró todas sus palabras; ninguna cosa se le escondió al rey que no le " +
            "declarase.",
        "Y cuando la reina de Sabá vio toda la sabiduría de Salomón, y la casa que había edificado,",
        "y las viandas de su mesa, y el asiento de sus siervos, y el estado de sus criados, y sus " +
            "vestidos, y sus maestresalas,",
        "y su escalera por donde subía a la casa de Jehová, quedó fuera de sí.",
        "Y dijo al rey: Verdad es lo que oí en mi tierra de tus cosas y de tu sabiduría.",
        "Mas yo no lo creía, hasta que he venido, y mis ojos han visto; y he aquí que ni aun la " +
            "mitad fue lo que se me dijo.",
        "Es mayor tu sabiduría y bien que la fama que yo había oído.",
        "Bienaventurados tus varones, dichosos estos tus siervos, que están continuamente delante " +
            "de ti, y oyen tu sabiduría.",
        "Jehová tu Dios sea bendito, que se agradó de ti para ponerte en el trono de Israel.",
    ),
)

internal fun readUiState(locale: String): ReadUiState.Success = ReadUiState.Success(
    chapterNumber = CHAPTER,
    bookStringResource = BookId.FIRST_KI.toBookNameResource(),
    isChapterRead = false,
    navigationSuggestions = ReadNavigationSuggestionsModel(
        previous = ReadNavigationSuggestionModel(
            bookId = BookId.SECOND_SA,
            chapterNumber = LAST_CHAPTER_OF_PREVIOUS_BOOK,
        ),
        next = ReadNavigationSuggestionModel(
            bookId = BookId.FIRST_KI,
            chapterNumber = CHAPTER + 1,
        ),
    ),
    verses = versesByLocale.getValue(locale).mapIndexed { index, text ->
        VerseUiModel(
            number = index + 1,
            text = text,
            isSelected = false,
        )
    },
)
