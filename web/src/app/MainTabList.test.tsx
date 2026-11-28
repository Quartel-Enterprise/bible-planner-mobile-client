import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { MainTabList } from './MainTabList'

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

    expect(screen.getAllByRole('button').map((button) => button.textContent)).toEqual(['Plans', 'Books', 'Ana'])
  })

  it('labels the profile tab Profile when signed out', () => {
    render(<MainTabList tabs={[{ key: 'profile', profileName: null }]} />)

    expect(screen.getByRole('button').textContent).toBe('Profile')
  })

  it('labels the profile tab Profile when the signed in name is blank', () => {
    render(<MainTabList tabs={[{ key: 'profile', profileName: '  ' }]} />)

    expect(screen.getByRole('button').textContent).toBe('Profile')
  })
})
