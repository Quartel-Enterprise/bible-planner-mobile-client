# chapter_study_outline_clicked

**Tier:** P2 | **Domain:** ChapterStudy

Captures the reader using the study's outline to jump back into the text. It measures whether the study sends people back to the chapter instead of replacing it.

## When it fires

The reader taps a section of the study's outline. The study closes and the reader below scrolls to that section's verses and flashes them.

## Trigger source

`feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `ChapterStudyUiEvent.OnOutlineSectionClick` → `onOutlineSectionClick(...)`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
