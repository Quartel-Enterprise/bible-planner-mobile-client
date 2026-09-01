package com.quare.bibleplanner.feature.chat.screenshots

import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatInputMode
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiState

/**
 * One exchange about Genesis 1-3, the reading the day, read and study screenshots also land on.
 *
 * The thread stops at the answer on purpose: a screenshot renders the list from the top without
 * scrolling, so whatever comes first is the whole image, and a trailing unanswered question would
 * read as a chat that broke rather than one that works.
 *
 * The history stays empty for a duller reason: it is a closed drawer at every size these render at,
 * because the frame measures its content narrower than the sidebar's 840dp breakpoint — the same
 * reason the day study screenshots hand their screen `isWide` by hand. Nothing in it is composed.
 */
private val contextLabelByLocale = mapOf(
    "en-US" to "Genesis 1-3",
    "pt-BR" to "Gênesis 1-3",
    "es" to "Génesis 1-3",
)
private val questionByLocale = mapOf(
    "en-US" to "If everything God made was good, where did the serpent come from?",
    "pt-BR" to "Se tudo o que Deus fez era bom, de onde veio a serpente?",
    "es" to "Si todo lo que Dios hizo era bueno, ¿de dónde salió la serpiente?",
)
private val answerByLocale = mapOf(
    "en-US" to "Genesis introduces the serpent without explaining it. Chapter 3 calls it the most " +
        "crafty of the wild animals God had made, and says nothing at all about where it came " +
        "from.\n\nThat silence is deliberate. The chapter is far more interested in what the " +
        "couple does with the question the serpent asks than in the creature asking it — the " +
        "weight falls on the choice, not on the tempter.",
    "pt-BR" to "Gênesis apresenta a serpente sem explicá-la. O capítulo 3 a chama de o mais astuto " +
        "dos animais do campo que Deus tinha feito, e não diz absolutamente nada sobre de onde " +
        "ela veio.\n\nEsse silêncio é proposital. O capítulo se interessa muito mais pelo que o " +
        "casal faz com a pergunta da serpente do que pela criatura que pergunta — o peso está na " +
        "escolha, não no tentador.",
    "es" to "Génesis presenta a la serpiente sin explicarla. El capítulo 3 la llama la más astuta " +
        "de los animales del campo que Dios había hecho, y no dice nada sobre de dónde " +
        "salió.\n\nEse silencio es deliberado. Al capítulo le importa mucho más lo que la pareja " +
        "hace con la pregunta de la serpiente que la criatura que la formula — el peso está en la " +
        "elección, no en el tentador.",
)
private val suggestionsByLocale = mapOf(
    "en-US" to listOf(
        "Why two creation accounts?",
        "What does \"image of God\" mean?",
        "Where is Eden?",
    ),
    "pt-BR" to listOf(
        "Por que dois relatos da criação?",
        "O que significa \"imagem de Deus\"?",
        "Onde ficava o Éden?",
    ),
    "es" to listOf(
        "¿Por qué dos relatos de la creación?",
        "¿Qué significa \"imagen de Dios\"?",
        "¿Dónde estaba el Edén?",
    ),
)
private val emptyHistory = ChatHistoryUiState(
    isOpen = false,
    query = "",
    groups = emptyList(),
    hasConversations = false,
    expandedActionsId = null,
    renamingId = null,
    renameDraft = "",
    deletingId = null,
)

internal fun chatUiState(locale: String): ChatUiState = ChatUiState(
    contextLabel = contextLabelByLocale.getValue(locale),
    messages = listOf(
        ChatMessageUiModel(
            id = "question",
            text = questionByLocale.getValue(locale),
            isFromUser = true,
            isStreaming = false,
            isFailed = false,
        ),
        ChatMessageUiModel(
            id = "answer",
            text = answerByLocale.getValue(locale),
            isFromUser = false,
            isStreaming = false,
            isFailed = false,
        ),
    ),
    pendingQuestion = null,
    suggestions = suggestionsByLocale.getValue(locale),
    isSuggestionBarExpanded = false,
    input = "",
    inputMode = ChatInputMode.ENABLED,
    cooldownSeconds = 0,
    // A subscriber has no free-question counter, which is the state the listing should advertise.
    quota = null,
    isThinking = false,
    isAnswering = false,
    failure = null,
    history = emptyHistory,
)
