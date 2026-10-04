# chapter_listening_back_to_verse_clicked

**Tier:** P2 | **Domain:** Listening

The person brought the reader back to the verse being read after scrolling away.

## When it fires

Tap on the Back to the current verse chip.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `OnBackToVerseClick`.

## Parameters

None.

## Notes

- The chip only shows after the person scrolled the verse being read out of view, which pauses the automatic scrolling.
