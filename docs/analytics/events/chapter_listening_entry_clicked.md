# chapter_listening_entry_clicked

**Tier:** P1 | **Domain:** Listening

The person tapped one of the listen entry points of the reader.

## When it fires

Tap on the headphones button of the bottom bar (phone), of the header (tablet/desktop), or on the shortcut under the chapter number (phone in vertical reading only). The bottom bar and header buttons always act on the chapter on screen.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `ReadListeningUiEvent.OnListenClick` (manual, since `is_active` depends on the player state).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `bottom_bar` | `bottom_bar` \| `header` \| `shortcut` |
| `is_active` | string | `"false"` | Whether that chapter was already playing; when it was, the tap opens the player instead of starting |

## Notes

- A free user without the chapter unlocked goes on to the unlock sheet ([unlock_sheet_viewed](unlock_sheet_viewed.md) with `surface=chapter_listening`) or the Pro teaser.
