import { describe, expect, it } from 'vitest'

import { applyDocumentLocale } from './locale'
import { locales, messages, resolveLocale } from './messages'

describe('resolveLocale', () => {
  it('picks the first language the app has', () => {
    expect(resolveLocale(['fr-FR', 'es-MX', 'en-US'])).toBe('es')
  })

  it('maps any Portuguese to pt-BR', () => {
    expect(resolveLocale(['pt-PT'])).toBe('pt-BR')
    expect(resolveLocale(['pt'])).toBe('pt-BR')
  })

  it('matches regardless of case', () => {
    expect(resolveLocale(['PT-br'])).toBe('pt-BR')
  })

  it('falls back to English when none matches', () => {
    expect(resolveLocale(['fr-FR', 'de'])).toBe('en')
    expect(resolveLocale([])).toBe('en')
  })
})

describe('messages', () => {
  it('has every string in every locale', () => {
    for (const locale of locales) {
      const { tabs, error, ...rest } = messages[locale]
      for (const value of [...Object.values(rest), ...Object.values(tabs), ...Object.values(error)]) {
        expect(value.trim()).not.toBe('')
      }
    }
  })
})

describe('applyDocumentLocale', () => {
  it('sets the document language', () => {
    applyDocumentLocale('pt-BR')

    expect(document.documentElement.lang).toBe('pt-BR')
  })
})
