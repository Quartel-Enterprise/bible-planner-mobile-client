import { useEffect, useRef, useState } from 'react'

/** What a presenter's observe function returns: the shared library's JsWatch, or a test's fake. */
export interface Watch {
  cancel(): void
}

/** A presenter of the shared library: it owns one ViewModelHolder and clears it on dispose(). */
export interface Disposable {
  dispose(): void
}

/**
 * Keeps one shared ViewModel, through its presenter, for the life of a screen and disposes it when the screen goes
 * away, which clears its ViewModelHolder and cancels its viewModelScope like Android does. It is created in an
 * effect, so StrictMode's mount, unmount and mount never leaves a disposed presenter behind.
 */
export function usePresenter<P extends Disposable>(create: () => P): P | null {
  const [presenter, setPresenter] = useState<P | null>(null)
  // Why: the presenter belongs to the mount, so a new create function on a later render doesn't build another.
  const createRef = useRef(create)
  useEffect(() => {
    const created = createRef.current()
    setPresenter(created)
    return () => {
      created.dispose()
      setPresenter(null)
    }
  }, [])
  return presenter
}

/**
 * Mirrors one of a presenter's flows into React state: null until its first value. The watch is cancelled when the
 * presenter changes or the screen goes away.
 */
export function useWatch<P, S>(
  presenter: P | null,
  observe: (presenter: P, onChange: (value: S) => void) => Watch,
): S | null {
  const [value, setValue] = useState<S | null>(null)
  // Why: observe only picks which flow to watch, so an inline arrow function doesn't restart the watch each render.
  const observeRef = useRef(observe)
  useEffect(() => {
    if (!presenter) return
    const watch = observeRef.current(presenter, (next) => {
      setValue(() => next)
    })
    return () => {
      watch.cancel()
      setValue(null)
    }
  }, [presenter])
  return value
}
