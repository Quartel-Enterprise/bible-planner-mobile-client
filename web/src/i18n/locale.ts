import { resolveLocale, type Locale } from './messages'

/*
 * Why: the browser's languages, not the app's language setting: that one lives in the shared DataStore and is read
 * asynchronously, while the first frame (and the error page) already needs its strings. A screen that reads the
 * setting through its presenter can switch later.
 */
export function detectLocale(): Locale {
  return resolveLocale(navigator.languages.length > 0 ? navigator.languages : [navigator.language])
}

/** Sets <html lang> to the locale, for screen readers, hyphenation and the browser's translate offer. */
export function applyDocumentLocale(locale: Locale) {
  document.documentElement.lang = locale
}
