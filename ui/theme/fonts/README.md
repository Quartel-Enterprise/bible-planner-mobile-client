# Bundled reading fonts

The reader lets the user pick the typeface the bible text is set in, and the share-as-image card
offers a second, display-oriented list. Both ship with the app so they work offline and render the
same on every platform.

The files live in `../src/commonMain/composeResources/font/` and are wired up by `ReaderFont` and
`ShareCardFont` (`../src/commonMain/kotlin/.../font/`).

Every bundled family is licensed under the **SIL Open Font License 1.1** ([`OFL.txt`](OFL.txt)),
which permits bundling and redistribution inside an application as long as the fonts are not sold on
their own and the license travels with them.

| File | Family | Used by | Source |
|---|---|---|---|
| `lora.ttf` | Lora | reader + share card | [Google Fonts](https://fonts.google.com/specimen/Lora) |
| `literata.ttf` | Literata | reader | [Google Fonts](https://fonts.google.com/specimen/Literata) |
| `eb_garamond.ttf` | EB Garamond | reader + share card | [Google Fonts](https://fonts.google.com/specimen/EB+Garamond) |
| `crimson_pro.ttf` | Crimson Pro | reader | [Google Fonts](https://fonts.google.com/specimen/Crimson+Pro) |
| `bitter.ttf` | Bitter | reader | [Google Fonts](https://fonts.google.com/specimen/Bitter) |
| `nunito_sans.ttf` | Nunito Sans | reader | [Google Fonts](https://fonts.google.com/specimen/Nunito+Sans) |
| `atkinson_hyperlegible.ttf` | Atkinson Hyperlegible | reader | [Google Fonts](https://fonts.google.com/specimen/Atkinson+Hyperlegible) |
| `open_dyslexic.otf` | OpenDyslexic | reader | [antijingoist/opendyslexic](https://github.com/antijingoist/opendyslexic) |
| `playfair_display.ttf` | Playfair Display | share card | [Google Fonts](https://fonts.google.com/specimen/Playfair+Display) |
| `dm_serif_display.ttf` | DM Serif Display | share card | [Google Fonts](https://fonts.google.com/specimen/DM+Serif+Display) |
| `caveat.ttf` | Caveat | share card | [Google Fonts](https://fonts.google.com/specimen/Caveat) |
| `montserrat.ttf` | Montserrat | share card | [Google Fonts](https://fonts.google.com/specimen/Montserrat) |

The reader's ninth option, **Roboto**, ships no file: it maps to the platform's own sans-serif, which
is Roboto on Android and the closest system equivalent elsewhere.

## Why these files and not the ones in the `google/fonts` repository

The repository publishes these families as variable fonts covering every script they support — from
212 KB (Lora) to 955 KB (Literata), about 4.7 MB for the set. What is bundled here instead are the
static Regular instances of the **latin** subset that the Google Fonts CSS API serves, ~800 KB for
all twelve. They cover Latin-1 (so Portuguese, Spanish and English render in full) and each carries a
single weight; bold and italic are synthesised by the text engine, which is what the reader's design
asks for anyway.

To refresh a file, request it from the CSS API with a user agent old enough that it is offered
TrueType rather than WOFF2, and take the URL for the family you want:

```bash
curl -H "User-Agent: Mozilla/4.0" "https://fonts.googleapis.com/css?family=Lora"
```
