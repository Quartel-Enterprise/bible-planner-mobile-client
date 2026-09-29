package com.quare.bibleplanner.feature.verse.annotations.presentation.factory

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsFilters
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.BookFilterUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.ColorFilterUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.DatedAnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.PeriodFilterUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.TypeFilterUiModel
import com.quare.bibleplanner.ui.utils.removeAccents
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

internal class AnnotationsContentFactory(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    fun create(
        entries: List<AnnotationEntry>,
        otherVersionCounts: List<VersionAnnotationCount>,
        filters: AnnotationsFilters,
    ): AnnotationsContentUiModel {
        val today = toLocalDate(currentTimestampProvider.getCurrentTimestamp())
        val datedEntries = entries.map { entry ->
            val date = toLocalDate(entry.passage.updatedAtEpochMillis)
            DatedAnnotationEntry(
                entry = entry,
                date = date,
                daysAgo = date.daysUntil(today),
            )
        }
        val normalizedQuery = filters.query.trim().normalizeForSearch()
        val shownEntries = datedEntries.filter { dated ->
            dated.matchesQuery(normalizedQuery) &&
                dated.matchesType(filters.type) &&
                dated.matchesColor(filters.color) &&
                dated.matchesBook(filters.bookId) &&
                dated.matchesPeriod(
                    period = filters.period,
                    customRange = filters.customRange,
                )
        }
        return AnnotationsContentUiModel(
            totalCount = entries.size,
            shownCount = shownEntries.size,
            typeFilters = createTypeFilters(
                datedEntries = datedEntries,
                selectedType = filters.type,
            ),
            colorFilters = createColorFilters(
                datedEntries = datedEntries,
                filters = filters,
            ),
            bookFilters = createBookFilters(
                datedEntries = datedEntries,
                filters = filters,
            ),
            periodFilters = createPeriodFilters(
                datedEntries = datedEntries,
                filters = filters,
            ),
            selectedBookId = filters.bookId,
            selectedPeriod = filters.period,
            customRange = filters.customRange,
            today = today,
            hasActiveFilters = filters != noFilters,
            groups = createGroups(
                shownEntries = shownEntries,
                today = today,
            ),
            otherVersions = otherVersionCounts
                .sortedByDescending { it.count }
                .map(::toOtherVersion),
        )
    }

    private fun toOtherVersion(versionCount: VersionAnnotationCount): OtherVersionAnnotationsUiModel =
        OtherVersionAnnotationsUiModel(
            bibleVersionId = versionCount.bibleVersionId,
            versionAbbreviation = versionCount.bibleVersionId.uppercase(),
            count = versionCount.count,
        )

    private fun createTypeFilters(
        datedEntries: List<DatedAnnotationEntry>,
        selectedType: AnnotationTypeFilter,
    ): List<TypeFilterUiModel> = AnnotationTypeFilter.entries.map { type ->
        TypeFilterUiModel(
            type = type,
            count = datedEntries.count { it.matchesType(type) },
            isSelected = type == selectedType,
        )
    }

    private fun createColorFilters(
        datedEntries: List<DatedAnnotationEntry>,
        filters: AnnotationsFilters,
    ): List<ColorFilterUiModel> {
        val presetColors = PresetHighlightColor.entries.map(HighlightColor::Preset)
        val customColors = datedEntries
            .mapNotNull { it.entry.passage.highlightColor as? HighlightColor.Custom }
            .distinctBy { it.key }
        val candidates = datedEntries.filter { dated ->
            dated.matchesType(filters.type) &&
                dated.matchesBook(filters.bookId) &&
                dated.matchesPeriod(
                    period = filters.period,
                    customRange = filters.customRange,
                )
        }
        return (presetColors + customColors)
            .map { color ->
                ColorFilterUiModel(
                    color = color,
                    count = candidates.count { it.matchesColor(color) },
                    isSelected = color.key == filters.color?.key,
                )
            }.sortedByDescending { it.count }
    }

    private fun createBookFilters(
        datedEntries: List<DatedAnnotationEntry>,
        filters: AnnotationsFilters,
    ): List<BookFilterUiModel> {
        val candidates = datedEntries.filter { dated ->
            dated.matchesType(filters.type) &&
                dated.matchesColor(filters.color) &&
                dated.matchesPeriod(
                    period = filters.period,
                    customRange = filters.customRange,
                )
        }
        val bookIds = datedEntries
            .map { it.entry.passage.chapter.bookId }
            .distinct()
            .sortedBy { it.ordinal }
        return (listOf(null) + bookIds).map { bookId ->
            BookFilterUiModel(
                bookId = bookId,
                count = candidates.count { it.matchesBook(bookId) },
                isSelected = bookId == filters.bookId,
            )
        }
    }

    private fun createPeriodFilters(
        datedEntries: List<DatedAnnotationEntry>,
        filters: AnnotationsFilters,
    ): List<PeriodFilterUiModel> {
        val candidates = datedEntries.filter { dated ->
            dated.matchesType(filters.type) &&
                dated.matchesColor(filters.color) &&
                dated.matchesBook(filters.bookId)
        }
        return AnnotationPeriod.entries.map { period ->
            PeriodFilterUiModel(
                period = period,
                count = candidates.count { dated ->
                    dated.matchesPeriod(
                        period = period,
                        customRange = filters.customRange,
                    )
                },
                isSelected = period == filters.period,
            )
        }
    }

    private fun createGroups(
        shownEntries: List<DatedAnnotationEntry>,
        today: LocalDate,
    ): List<AnnotationGroupUiModel> = shownEntries
        .groupBy { dated -> dated.toGroupLabel(today) }
        .map { (label, groupEntries) ->
            AnnotationGroupUiModel(
                label = label,
                items = groupEntries.map { it.entry.toItem() },
            )
        }

    private fun DatedAnnotationEntry.toGroupLabel(today: LocalDate): AnnotationGroupLabel = when (daysAgo) {
        0 -> AnnotationGroupLabel.Today

        1 -> AnnotationGroupLabel.Yesterday

        else -> AnnotationGroupLabel.MonthOfYear(
            month = date.month,
            year = date.year.takeIf { it != today.year },
        )
    }

    private fun AnnotationEntry.toItem(): AnnotationItemUiModel = AnnotationItemUiModel(
        key = passage.toKey(),
        passage = passage,
        text = text,
        versionAbbreviation = passage.chapter.bibleVersionId.uppercase(),
    )

    private fun AnnotatedPassage.toKey(): String = listOf(
        chapter.bibleVersionId,
        chapter.bookId.name,
        chapter.chapterNumber,
        verseNumbers.joinToString(separator = ","),
        note?.id.orEmpty(),
    ).joinToString(separator = KEY_SEPARATOR)

    private fun DatedAnnotationEntry.matchesType(type: AnnotationTypeFilter): Boolean = when (type) {
        AnnotationTypeFilter.ALL -> true
        AnnotationTypeFilter.HIGHLIGHTS -> entry.passage.highlightColor != null
        AnnotationTypeFilter.SAVED -> entry.passage.isSaved
        AnnotationTypeFilter.NOTES -> entry.passage.note != null
    }

    private fun DatedAnnotationEntry.matchesColor(color: HighlightColor?): Boolean =
        color == null || entry.passage.highlightColor?.key == color.key

    private fun DatedAnnotationEntry.matchesBook(bookId: BookId?): Boolean =
        bookId == null || entry.passage.chapter.bookId == bookId

    private fun DatedAnnotationEntry.matchesPeriod(
        period: AnnotationPeriod,
        customRange: AnnotationDateRange?,
    ): Boolean = if (period == AnnotationPeriod.CUSTOM) {
        customRange?.let { range -> date in range.start..range.end } ?: true
    } else {
        period.maxDaysAgo?.let { maxDaysAgo -> daysAgo <= maxDaysAgo } ?: true
    }

    private fun DatedAnnotationEntry.matchesQuery(normalizedQuery: String): Boolean = normalizedQuery.isEmpty() ||
        listOfNotNull(
            entry.reference,
            entry.text,
            entry.passage.note?.text,
        ).any { field -> field.normalizeForSearch().contains(normalizedQuery) }

    private fun String.normalizeForSearch(): String = removeAccents().lowercase()

    private fun toLocalDate(timestamp: Long): LocalDate = localDateTimeProvider.getLocalDateTime(timestamp).date

    companion object {
        val noFilters = AnnotationsFilters(
            type = AnnotationTypeFilter.ALL,
            color = null,
            bookId = null,
            period = AnnotationPeriod.ANY,
            customRange = null,
            query = "",
        )
        private const val KEY_SEPARATOR = "|"
    }
}
