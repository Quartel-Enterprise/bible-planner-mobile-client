# verse_note_icon_clicked

**Tier:** P1 | **Domain:** Verse annotations

The note icon beside an annotated verse in the reader was tapped, opening that note. Measures whether people come back to their notes straight from the text, without selecting verses first.

## When it fires

The user taps the note icon (or the bar joining the verses of a longer note) beside a verse in the reader.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnNoteIconClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the note covers |

## Notes

- Only shown while the reader's Note icon setting is on (see [reader_note_icon_toggled](reader_note_icon_toggled.md)).
- The other ways into the same editor are [verse_note_opened](verse_note_opened.md) (selection panel) and [annotation_note_opened](annotation_note_opened.md) (annotations screen); all end in [verse_note_saved](verse_note_saved.md), [verse_note_deleted](verse_note_deleted.md) or [verse_note_dismissed](verse_note_dismissed.md).
