# chapter_study_cross_reference_clicked

**Tier:** P2 | **Domain:** ChapterStudy

Captures the reader following one of the study's cross references into another passage.

## When it fires

The reader taps a cross-reference chip. The reader opens on that chapter, scrolled to the referenced verses.

## Trigger source

`feature/chapter_study/.../presentation/model/ChapterStudyUiEvent.kt` — `OnCrossReferenceClick`, tracked automatically.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `ROM` | Book the reference points to (`BookId` name) |
| `chapter_number` | int | `5` | Chapter the reference points to |

## Notes

- The parameters name the destination, not the chapter being studied.
