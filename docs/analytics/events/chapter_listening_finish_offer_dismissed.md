# chapter_listening_finish_offer_dismissed

**Tier:** P2 | **Domain:** Listening

The offer to mark a chapter heard to the end as read was dismissed.

## When it fires

Tap on the close button of the offer card.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `OnFinishOfferDismissClick`.

## Parameters

None.

## Notes

- Accepting it is [chapter_read_toggled](chapter_read_toggled.md) with `source=listening_offer`.
