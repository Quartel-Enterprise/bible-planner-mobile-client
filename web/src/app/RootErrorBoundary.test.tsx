import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { I18nProvider } from '@/i18n/I18nProvider'
import { RootErrorBoundary } from './RootErrorBoundary'

function Throwing(): never {
  throw new Error('Koin could not build the ViewModel')
}

describe('RootErrorBoundary', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows the translated fallback instead of a blank page', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined)

    render(
      <I18nProvider locale="es">
        <RootErrorBoundary>
          <Throwing />
        </RootErrorBoundary>
      </I18nProvider>,
    )

    expect(screen.getByRole('alert').textContent).toContain('Algo salió mal')
    expect(screen.getByRole('button').textContent).toBe('Intentar de nuevo')
  })

  it('renders its children while nothing throws', () => {
    render(
      <RootErrorBoundary>
        <p>content</p>
      </RootErrorBoundary>,
    )

    expect(screen.getByText('content')).toBeTruthy()
  })
})
