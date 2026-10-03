package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyCrossReferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyNameEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyOutlineSectionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.relation.ChapterStudyWithContent

internal class ChapterStudyEntityMapper(
    private val wireNameBookIdMapper: WireNameBookIdMapper,
) {
    fun mapToEntities(
        cacheKey: String,
        response: ChapterStudyResponseDto,
    ): ChapterStudyWithContent = ChapterStudyWithContent(
        study = ChapterStudyEntity(
            cacheKey = cacheKey,
            summary = response.content.summary,
            context = response.content.context,
            keyVerseStart = response.content.keyVerse?.startVerse,
            keyVerseEnd = response.content.keyVerse?.endVerse,
            keyVerseNote = response.content.keyVerse?.note,
            model = response.model,
            promptVersion = response.promptVersion,
            updatedAt = response.updatedAt,
            cacheToken = response.clientCacheToken,
        ),
        outlineSections = response.content.outline.mapIndexed { index, section ->
            ChapterStudyOutlineSectionEntity(
                cacheKey = cacheKey,
                position = index,
                startVerse = section.startVerse,
                endVerse = section.endVerse,
                title = section.title,
                id = 0,
            )
        },
        names = response.content.peopleAndPlaces.mapIndexed { index, name ->
            ChapterStudyNameEntity(
                cacheKey = cacheKey,
                position = index,
                name = name,
                id = 0,
            )
        },
        crossReferences = response.content.crossReferences
            .mapNotNull { reference ->
                wireNameBookIdMapper.mapOrNull(reference.book)?.let { bookId -> bookId to reference }
            }.mapIndexed { index, (bookId, reference) ->
                ChapterStudyCrossReferenceEntity(
                    cacheKey = cacheKey,
                    position = index,
                    bookId = bookId.name,
                    chapterNumber = reference.chapter,
                    startVerse = reference.startVerse,
                    endVerse = reference.endVerse,
                    id = 0,
                )
            },
        questions = response.content.reflectionQuestions.mapIndexed { index, question ->
            ChapterStudyQuestionEntity(
                cacheKey = cacheKey,
                position = index,
                text = question,
                id = 0,
            )
        },
    )

    fun mapToDomain(content: ChapterStudyWithContent): ChapterStudyModel = ChapterStudyModel(
        summary = content.study.summary,
        context = content.study.context,
        outline = content.outlineSections
            .sortedBy(ChapterStudyOutlineSectionEntity::position)
            .map { entity ->
                OutlineSectionModel(
                    startVerse = entity.startVerse,
                    endVerse = entity.endVerse,
                    title = entity.title,
                )
            },
        peopleAndPlaces = content.names
            .sortedBy(ChapterStudyNameEntity::position)
            .map(ChapterStudyNameEntity::name),
        keyVerse = mapKeyVerseOrNull(content.study),
        crossReferences = content.crossReferences
            .sortedBy(ChapterStudyCrossReferenceEntity::position)
            .mapNotNull(::mapCrossReferenceOrNull),
        reflectionQuestions = content.questions
            .sortedBy(ChapterStudyQuestionEntity::position)
            .map(ChapterStudyQuestionEntity::text),
    )

    private fun mapKeyVerseOrNull(study: ChapterStudyEntity): KeyVerseModel? {
        val startVerse = study.keyVerseStart ?: return null
        return KeyVerseModel(
            startVerse = startVerse,
            endVerse = study.keyVerseEnd ?: startVerse,
            note = study.keyVerseNote.orEmpty(),
        )
    }

    private fun mapCrossReferenceOrNull(entity: ChapterStudyCrossReferenceEntity): CrossReferenceModel? {
        val bookId = BookId.entries.firstOrNull { it.name == entity.bookId } ?: return null
        return CrossReferenceModel(
            bookId = bookId,
            chapterNumber = entity.chapterNumber,
            startVerse = entity.startVerse,
            endVerse = entity.endVerse,
        )
    }
}
