import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { I18nProvider } from '@/i18n/I18nProvider'
import { MainTabList } from './MainTabList'

function itemTexts(): (string | null)[] {
  return screen.getAllByRole('listitem').map((item) => item.textContent)
}

describe('MainTabList', () => {
  it('labels each tab, and the profile tab with the signed in name', () => {
    render(
      <MainTabList
        tabs={[
          { key: 'plans', profileName: null },
          { key: 'books', profileName: null },
          { key: 'profile', profileName: 'Ana' },
        ]}
      />,
    )

    expect(itemTexts()).toEqual(['Plans', 'Books', 'Ana'])
  })

  it('labels the profile tab Profile when signed out', () => {
    render(<MainTabList tabs={[{ key: 'profile', profileName: null }]} />)

    expect(itemTexts()).toEqual(['Profile'])
  })

  it('labels the profile tab Profile when the signed in name is blank', () => {
    render(<MainTabList tabs={[{ key: 'profile', profileName: '  ' }]} />)

    expect(itemTexts()).toEqual(['Profile'])
  })

  it('labels the tabs in the provider locale', () => {
    render(
      <I18nProvider locale="pt-BR">
        <MainTabList
          tabs={[
            { key: 'plans', profileName: null },
            { key: 'books', profileName: null },
            { key: 'profile', profileName: null },
          ]}
        />
      </I18nProvider>,
    )

    expect(itemTexts()).toEqual(['Planos', 'Livros', 'Perfil'])
  })

  it('has nothing to click until navigation exists', () => {
    render(<MainTabList tabs={[{ key: 'plans', profileName: null }]} />)

    expect(screen.queryByRole('button')).toBeNull()
  })
})
