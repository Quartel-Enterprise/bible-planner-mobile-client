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
 * Study text for Genesis 1-3 — the same reading the plan and day screenshots land on.
 *
 * Written by hand rather than captured from the model, which the team accepted for the store
 * listing on the grounds that it is representative. If the generated studies ever change shape —
 * more chapter summaries, a different tone, extra sections — this is what has to follow, since the
 * images promise this layout.
 */
private val studyByLocale = mapOf(
    "en-US" to DayStudyModel(
        passageLabel = "Genesis 1-3",
        overview = "The Bible opens by speaking a world into order and then watching trust break " +
            "inside it. Six days end with a verdict that everything is good; by the third chapter " +
            "a question about God's motive has been answered wrongly, and the garden closes " +
            "behind the man and the woman.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Chapter 1 — Order out of the formless",
                body = "God speaks and the world takes shape day by day: light, sky, land, lights, " +
                    "creatures. Each day ends with the same verdict, and the sixth adds humanity, " +
                    "made in God's image and handed the world to look after.",
            ),
            ChapterSummaryModel(
                title = "Chapter 2 — The garden and the first limit",
                body = "The account narrows from the cosmos to one garden, one man and one tree. " +
                    "Work, a boundary and companionship arrive together, and being alone is the " +
                    "first thing the story calls not good.",
            ),
            ChapterSummaryModel(
                title = "Chapter 3 — What the question cost",
                body = "The serpent reframes the one command as something withheld, and the couple " +
                    "eats. What follows is not a thunderbolt but a chain of consequences — shame, " +
                    "blame, pain, thorns, and the way back barred.",
            ),
        ),
        takeaways = listOf(
            "The world is spoken into order, not fought into it.",
            "The first freedom arrived with the first limit.",
            "The fall begins as a question about God's motive.",
        ),
        context = HistoricalContextModel(
            body = "Genesis opens the Torah for readers who knew exile first-hand. Its account " +
                "answers the origin stories around it, where the world is made out of a battle " +
                "and people are made to do the gods' work.",
            facts = listOf(
                FactModel(label = "Period", value = "Origins"),
                FactModel(label = "Place", value = "Eden"),
                FactModel(label = "Genre", value = "Narrative prologue"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "Are the six days meant as calendar days?",
                answer = "The chapter is built as a week and reads like a liturgy. Readers have " +
                    "taken the days literally and figuratively since long before the modern " +
                    "argument, and the text's own concern is who made the world.",
            ),
            QaModel(
                question = "Why are there two creation accounts?",
                answer = "Chapter 1 moves from the cosmos inward and chapter 2 from one garden " +
                    "outward. They are two angles on the same beginning, and the second is what " +
                    "sets up the third.",
            ),
            QaModel(
                question = "What is the serpent?",
                answer = "Genesis calls it a creature of the field and says no more. Later " +
                    "tradition identifies it with the accuser, but chapter 3 keeps the weight on " +
                    "the choice the couple make.",
            ),
        ),
    ),
    "pt-BR" to DayStudyModel(
        passageLabel = "Gênesis 1-3",
        overview = "A Bíblia começa falando um mundo em ordem e depois vendo a confiança se " +
            "romper dentro dele. Seis dias terminam com o veredito de que tudo é bom; no terceiro " +
            "capítulo uma pergunta sobre a intenção de Deus é respondida errado, e o jardim se " +
            "fecha atrás do homem e da mulher.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Capítulo 1 — Ordem a partir do informe",
                body = "Deus fala e o mundo toma forma dia a dia: luz, céu, terra, luminares, " +
                    "criaturas. Cada dia termina com o mesmo veredito, e o sexto acrescenta a " +
                    "humanidade, feita à imagem de Deus e encarregada de cuidar do mundo.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 2 — O jardim e o primeiro limite",
                body = "O relato se estreita do cosmos para um jardim, um homem e uma árvore. " +
                    "Trabalho, fronteira e companhia chegam juntos, e estar sozinho é a primeira " +
                    "coisa que a história chama de não boa.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 3 — O preço da pergunta",
                body = "A serpente reapresenta a única ordem como algo negado, e o casal come. O " +
                    "que vem depois não é um raio, mas uma sequência de consequências — vergonha, " +
                    "acusação, dor, espinhos e o caminho de volta fechado.",
            ),
        ),
        takeaways = listOf(
            "O mundo é falado em ordem, não conquistado à força.",
            "A primeira liberdade veio junto com o primeiro limite.",
            "A queda começa como dúvida sobre a intenção de Deus.",
        ),
        context = HistoricalContextModel(
            body = "Gênesis abre a Torá para leitores que conheciam o exílio de perto. Seu relato " +
                "responde às histórias de origem ao redor, em que o mundo nasce de uma batalha e " +
                "as pessoas são feitas para o trabalho dos deuses.",
            facts = listOf(
                FactModel(label = "Período", value = "Origens"),
                FactModel(label = "Lugar", value = "Éden"),
                FactModel(label = "Gênero", value = "Prólogo narrativo"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "Os seis dias são dias de calendário?",
                answer = "O capítulo é construído como uma semana e soa como uma liturgia. " +
                    "Leitores tomaram os dias ao pé da letra e em sentido figurado muito antes da " +
                    "discussão moderna, e a preocupação do texto é quem fez o mundo.",
            ),
            QaModel(
                question = "Por que existem dois relatos da criação?",
                answer = "O capítulo 1 vai do cosmos para dentro e o capítulo 2 vai de um jardim " +
                    "para fora. São dois ângulos do mesmo começo, e o segundo é o que prepara o " +
                    "terceiro.",
            ),
            QaModel(
                question = "O que é a serpente?",
                answer = "Gênesis a chama de animal do campo e não diz mais. A tradição posterior " +
                    "a identifica com o acusador, mas o capítulo 3 mantém o peso sobre a escolha " +
                    "do casal.",
            ),
        ),
    ),
    "es" to DayStudyModel(
        passageLabel = "Génesis 1-3",
        overview = "La Biblia empieza hablando un mundo en orden y luego viendo cómo la confianza " +
            "se rompe dentro de él. Seis días terminan con el veredicto de que todo es bueno; en " +
            "el tercer capítulo una pregunta sobre la intención de Dios se responde mal, y el " +
            "jardín se cierra detrás del hombre y la mujer.",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Capítulo 1 — Orden a partir de lo informe",
                body = "Dios habla y el mundo toma forma día a día: luz, cielo, tierra, lumbreras, " +
                    "criaturas. Cada día termina con el mismo veredicto, y el sexto añade a la " +
                    "humanidad, hecha a imagen de Dios y encargada de cuidar el mundo.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 2 — El jardín y el primer límite",
                body = "El relato se estrecha del cosmos a un jardín, un hombre y un árbol. El " +
                    "trabajo, el límite y la compañía llegan juntos, y estar solo es lo primero " +
                    "que la historia llama no bueno.",
            ),
            ChapterSummaryModel(
                title = "Capítulo 3 — Lo que costó la pregunta",
                body = "La serpiente presenta el único mandato como algo negado, y la pareja come. " +
                    "Lo que sigue no es un rayo, sino una cadena de consecuencias — vergüenza, " +
                    "acusación, dolor, espinos y el camino de vuelta cerrado.",
            ),
        ),
        takeaways = listOf(
            "El mundo se habla en orden, no se conquista a la fuerza.",
            "La primera libertad llegó junto con el primer límite.",
            "La caída empieza como duda sobre la intención de Dios.",
        ),
        context = HistoricalContextModel(
            body = "Génesis abre la Torá para lectores que conocían el exilio de cerca. Su relato " +
                "responde a las historias de origen de alrededor, donde el mundo nace de una " +
                "batalla y las personas se hacen para el trabajo de los dioses.",
            facts = listOf(
                FactModel(label = "Período", value = "Orígenes"),
                FactModel(label = "Lugar", value = "Edén"),
                FactModel(label = "Género", value = "Prólogo narrativo"),
            ),
        ),
        commonQuestions = listOf(
            QaModel(
                question = "¿Los seis días son días de calendario?",
                answer = "El capítulo está construido como una semana y suena a liturgia. Hubo " +
                    "lectores que tomaron los días al pie de la letra y en sentido figurado mucho " +
                    "antes del debate moderno, y lo que al texto le importa es quién hizo el mundo.",
            ),
            QaModel(
                question = "¿Por qué hay dos relatos de la creación?",
                answer = "El capítulo 1 va del cosmos hacia dentro y el capítulo 2 va de un jardín " +
                    "hacia fuera. Son dos ángulos del mismo comienzo, y el segundo es el que " +
                    "prepara el tercero.",
            ),
            QaModel(
                question = "¿Qué es la serpiente?",
                answer = "Génesis la llama animal del campo y no dice más. La tradición posterior " +
                    "la identifica con el acusador, pero el capítulo 3 mantiene el peso sobre la " +
                    "elección de la pareja.",
            ),
        ),
    ),
)
private const val FREE_LIMIT = 3

internal fun dayStudyUiState(
    locale: String,
    platform: Platform,
): DayStudyRouteUiState {
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
        platform = platform,
    )
}

/** The question the questions-tab screenshot opens, taken from the same data the screen renders. */
internal fun firstQuestion(locale: String): String = studyByLocale
    .getValue(locale)
    .commonQuestions
    .first()
    .question
