package com.quare.bibleplanner.feature.daystudy.screenshots

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.FactModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.model.QaModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState

/**
 * Placeholder study text for 1 Kings 10-12 — the same reading the plan and day screenshots land on.
 * It is written by hand, not produced by the model, so it stands in for the shape of a study rather
 * than for what the app actually generates. Replace it with a captured real generation before any
 * of these images go to a store listing.
 */
private val studyByLocale = mapOf(
    "en-US" to DayStudyModel(
        passageLabel = "1 Kings 10-12",
        overview = "Solomon's reign reaches its height and then unravels. The wealth and wisdom " +
            "that draw a foreign queen to Jerusalem are the same things his heart drifts away " +
            "with, and within a single generation the kingdom his father united splits in two.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Chapter 10 — The queen of Sheba",
                body = "A visiting queen tests Solomon with hard questions and leaves overwhelmed " +
                    "by his wisdom and his court. The chapter piles up gold, ivory and horses, " +
                    "describing a prosperity the earlier warnings about kings had cautioned against.",
            ),
            ChapterSummaryModel(
                title = "Chapter 11 — A divided heart",
                body = "Solomon's foreign wives turn him toward other gods, and the narrator names " +
                    "this plainly as the failure of his old age. God raises up adversaries and " +
                    "announces that the kingdom will be torn away from his son.",
            ),
            ChapterSummaryModel(
                title = "Chapter 12 — The kingdom splits",
                body = "Rehoboam rejects the elders' counsel to lighten his father's burden and " +
                    "answers the northern tribes harshly. Ten tribes follow Jeroboam instead, and " +
                    "Israel and Judah go their separate ways.",
            ),
        ),
        takeaways = listOf(
            "Prosperity did not protect Solomon from drifting.",
            "The split had a slow cause and a sudden trigger.",
            "Refusing counsel cost Rehoboam most of a kingdom.",
        ),
        context = HistoricalContextModel(
            body = "These chapters close the united monarchy. Written from Judah's perspective, " +
                "they explain a division their first readers already lived under.",
            facts = listOf(
                FactModel(label = "Period", value = "10th century BC"),
                FactModel(label = "Place", value = "Jerusalem and Shechem"),
                FactModel(label = "Genre", value = "Historical narrative"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "Why is Solomon's wealth described as a problem?",
                answer = "Deuteronomy had warned that a king should not multiply gold, horses or " +
                    "wives. Chapter 10 lists all three, so the abundance reads as a warning.",
            ),
            QaModel(
                question = "Was the division a punishment or a political failure?",
                answer = "The text presents both: God announces it in chapter 11, and Rehoboam " +
                    "brings it about through his own decision in chapter 12.",
            ),
            QaModel(
                question = "What happened to the two kingdoms afterwards?",
                answer = "Judah kept Jerusalem and David's line; Israel, the ten northern tribes, " +
                    "set up its own worship at Bethel and Dan. They stay separate for the rest of " +
                    "the book.",
            ),
        ),
    ),
    "pt-BR" to DayStudyModel(
        passageLabel = "1 Reis 10-12",
        overview = "O reinado de Salomão chega ao auge e então se desfaz. A riqueza e a sabedoria " +
            "que atraem uma rainha estrangeira a Jerusalém são as mesmas coisas com que o coração " +
            "dele se desvia, e em uma única geração o reino que seu pai uniu se parte em dois.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Capítulo 10 — A rainha de Sabá",
                body = "Uma rainha visitante testa Salomão com perguntas difíceis e sai " +
                    "impressionada com sua sabedoria e sua corte. O capítulo acumula ouro, marfim " +
                    "e cavalos, descrevendo a prosperidade contra a qual os antigos avisos sobre " +
                    "reis já alertavam.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 11 — Um coração dividido",
                body = "As mulheres estrangeiras de Salomão o inclinam a outros deuses, e o " +
                    "narrador nomeia isso como o fracasso da sua velhice. Deus levanta adversários " +
                    "e anuncia que o reino será arrancado do filho dele.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 12 — O reino se divide",
                body = "Roboão rejeita o conselho dos anciãos de aliviar o fardo do pai e responde " +
                    "com dureza às tribos do norte. Dez tribos seguem Jeroboão, e Israel e Judá " +
                    "tomam caminhos separados.",
            ),
        ),
        takeaways = listOf(
            "A prosperidade não impediu Salomão de se desviar.",
            "A divisão teve causa lenta e estopim repentino.",
            "Recusar conselho custou a Roboão quase todo o reino.",
        ),
        context = HistoricalContextModel(
            body = "Estes capítulos encerram a monarquia unida. Escritos da perspectiva de Judá, " +
                "explicam uma divisão que seus primeiros leitores já viviam.",
            facts = listOf(
                FactModel(label = "Período", value = "Século X a.C."),
                FactModel(label = "Lugar", value = "Jerusalém e Siquém"),
                FactModel(label = "Gênero", value = "Narrativa histórica"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "Por que a riqueza de Salomão é descrita como problema?",
                answer = "Deuteronômio advertia que um rei não deveria multiplicar ouro, cavalos " +
                    "nem mulheres. O capítulo 10 lista os três, então a abundância soa como alerta.",
            ),
            QaModel(
                question = "A divisão foi castigo ou falha política?",
                answer = "O texto apresenta as duas coisas: Deus a anuncia no capítulo 11, e Roboão " +
                    "a provoca por decisão própria no capítulo 12.",
            ),
            QaModel(
                question = "O que aconteceu com os dois reinos depois?",
                answer = "Judá ficou com Jerusalém e a linhagem de Davi; Israel, as dez tribos do " +
                    "norte, montou seu próprio culto em Betel e Dã. Seguem separados pelo resto " +
                    "do livro.",
            ),
        ),
    ),
    "es" to DayStudyModel(
        passageLabel = "1 Reyes 10-12",
        overview = "El reinado de Salomón llega a su apogeo y luego se deshace. La riqueza y la " +
            "sabiduría que atraen a una reina extranjera a Jerusalén son aquello con lo que su " +
            "corazón se desvía, y en una sola generación el reino que unió su padre se parte en dos.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Capítulo 10 — La reina de Sabá",
                body = "Una reina de visita pone a prueba a Salomón con preguntas difíciles y se " +
                    "marcha asombrada por su sabiduría y su corte. El capítulo acumula oro, marfil " +
                    "y caballos, describiendo la prosperidad contra la que ya advertían los " +
                    "antiguos avisos sobre los reyes.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 11 — Un corazón dividido",
                body = "Las mujeres extranjeras de Salomón lo inclinan hacia otros dioses, y el " +
                    "narrador lo señala como el fracaso de su vejez. Dios levanta adversarios y " +
                    "anuncia que el reino le será arrancado a su hijo.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 12 — El reino se divide",
                body = "Roboam rechaza el consejo de los ancianos de aliviar la carga de su padre " +
                    "y responde con dureza a las tribus del norte. Diez tribus siguen a Jeroboam, " +
                    "e Israel y Judá toman caminos separados.",
            ),
        ),
        takeaways = listOf(
            "La prosperidad no impidió que Salomón se desviara.",
            "La división tuvo causa lenta y detonante repentino.",
            "Rechazar el consejo le costó a Roboam casi todo el reino.",
        ),
        context = HistoricalContextModel(
            body = "Estos capítulos cierran la monarquía unida. Escritos desde la perspectiva de " +
                "Judá, explican una división que sus primeros lectores ya vivían.",
            facts = listOf(
                FactModel(label = "Período", value = "Siglo X a.C."),
                FactModel(label = "Lugar", value = "Jerusalén y Siquem"),
                FactModel(label = "Género", value = "Narrativa histórica"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "¿Por qué la riqueza de Salomón se describe como un problema?",
                answer = "Deuteronomio advertía que un rey no debía multiplicar oro, caballos ni " +
                    "mujeres. El capítulo 10 enumera los tres, así que la abundancia suena a aviso.",
            ),
            QaModel(
                question = "¿La división fue castigo o fallo político?",
                answer = "El texto presenta ambas cosas: Dios la anuncia en el capítulo 11, y " +
                    "Roboam la provoca por decisión propia en el capítulo 12.",
            ),
            QaModel(
                question = "¿Qué pasó después con los dos reinos?",
                answer = "Judá conservó Jerusalén y la línea de David; Israel, las diez tribus del " +
                    "norte, montó su propio culto en Betel y Dan. Siguen separados el resto del " +
                    "libro.",
            ),
        ),
    ),
)

private const val FREE_LIMIT = 3

internal fun dayStudyUiState(locale: String): DayStudyRouteUiState {
    val study = studyByLocale.getValue(locale)
    return DayStudyRouteUiState(
        card = Loadable.Loaded(
            DayStudyCardUiModel(
                mode = DayStudyCardMode.VIEW,
                quota = Loadable.Loaded(
                    DayStudyCardQuotaUiModel(
                        remainingFree = FREE_LIMIT,
                        freeLimit = FREE_LIMIT,
                    ),
                ),
                isPro = true,
            ),
        ),
        generation = null,
        generationError = null,
        openStudy = study,
        isOpeningStudy = false,
        passageLabel = study.passageLabel,
        platform = Platform.Android,
    )
}

/** The question the questions-tab screenshot opens, taken from the same data the screen renders. */
internal fun firstQuestion(locale: String): String = studyByLocale
    .getValue(locale)
    .commonQuestions
    .first()
    .question
