import { StrictMode } from 'react'
import { act, render, renderHook, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { usePresenter, useWatch, type Watch } from './usePresenter'

/** Stands in for a shared library presenter: a FlowWatcher behind observe, a ViewModelHolder behind dispose. */
class FakePresenter {
  disposeCount = 0
  private listeners = new Set<(value: string) => void>()
  cancelCount = 0

  get activeWatchCount(): number {
    return this.listeners.size
  }

  observe(onChange: (value: string) => void): Watch {
    this.listeners.add(onChange)
    return {
      cancel: () => {
        this.cancelCount++
        this.listeners.delete(onChange)
      },
    }
  }

  emit(value: string) {
    this.listeners.forEach((listener) => {
      listener(value)
    })
  }

  dispose() {
    this.disposeCount++
  }
}

function Screen({ created }: { created: FakePresenter[] }) {
  const presenter = usePresenter(() => {
    const next = new FakePresenter()
    created.push(next)
    return next
  })
  const value = useWatch(presenter, (p, onChange: (value: string) => void) => p.observe(onChange))
  return <p>{value ?? 'loading'}</p>
}

describe('usePresenter', () => {
  it('creates one presenter and disposes it on unmount', () => {
    const created: FakePresenter[] = []
    const { result, rerender, unmount } = renderHook(() =>
      usePresenter(() => {
        const next = new FakePresenter()
        created.push(next)
        return next
      }),
    )

    rerender()

    expect(created).toHaveLength(1)
    expect(result.current).toBe(created[0])
    unmount()
    expect(created[0]?.disposeCount).toBe(1)
  })

  it('leaves no disposed presenter in use under StrictMode', () => {
    const created: FakePresenter[] = []
    const { unmount } = render(
      <StrictMode>
        <Screen created={created} />
      </StrictMode>,
    )

    const live = created.filter((presenter) => presenter.disposeCount === 0)
    expect(live).toHaveLength(1)
    expect(live[0]?.activeWatchCount).toBe(1)
    unmount()
    expect(created.every((presenter) => presenter.disposeCount === 1)).toBe(true)
  })
})

describe('useWatch', () => {
  it('is null before the first value, then mirrors each value', () => {
    const created: FakePresenter[] = []
    render(<Screen created={created} />)

    expect(screen.getByText('loading')).toBeTruthy()
    act(() => {
      created[0]?.emit('first')
    })
    expect(screen.getByText('first')).toBeTruthy()
    act(() => {
      created[0]?.emit('second')
    })
    expect(screen.getByText('second')).toBeTruthy()
  })

  it('does not watch while there is no presenter', () => {
    let observed = false
    const { result } = renderHook(() =>
      useWatch(null as FakePresenter | null, (p, onChange: (value: string) => void) => {
        observed = true
        return p.observe(onChange)
      }),
    )

    expect(result.current).toBeNull()
    expect(observed).toBe(false)
  })

  it('cancels the watch on unmount', () => {
    const created: FakePresenter[] = []
    const { unmount } = render(<Screen created={created} />)

    unmount()

    expect(created[0]?.cancelCount).toBe(1)
    expect(created[0]?.activeWatchCount).toBe(0)
  })

  it('watches once across renders with an inline observe function', () => {
    const presenter = new FakePresenter()
    const { rerender } = renderHook(() =>
      useWatch(presenter, (p, onChange: (value: string) => void) => p.observe(onChange)),
    )

    rerender()
    rerender()

    expect(presenter.activeWatchCount).toBe(1)
    expect(presenter.cancelCount).toBe(0)
  })

  it('moves the watch to a new presenter and drops the old value', () => {
    const first = new FakePresenter()
    const second = new FakePresenter()
    const { result, rerender } = renderHook(
      ({ presenter }) => useWatch(presenter, (p, onChange: (value: string) => void) => p.observe(onChange)),
      { initialProps: { presenter: first } },
    )
    act(() => {
      first.emit('old')
    })

    rerender({ presenter: second })

    expect(first.activeWatchCount).toBe(0)
    expect(second.activeWatchCount).toBe(1)
    expect(result.current).toBeNull()
  })
})
