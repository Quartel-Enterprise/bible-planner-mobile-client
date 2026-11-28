import { Component, type ErrorInfo, type ReactNode } from 'react'

import { Button } from '@/components/ui/button'
import { useMessages } from '@/i18n/I18nProvider'

/** What the page shows instead of going blank: the shared app failed to start or a screen threw. */
export function ErrorFallback() {
  const { error } = useMessages()
  return (
    <main role="alert" className="mx-auto flex min-h-svh max-w-md flex-col items-center justify-center gap-4 px-4 text-center">
      <p>{error.message}</p>
      <Button
        onClick={() => {
          window.location.reload()
        }}
      >
        {error.retry}
      </Button>
    </main>
  )
}

interface State {
  hasError: boolean
}

/*
 * Why: a presenter throws when Koin can't build its ViewModel; without a boundary React unmounts the whole tree and
 * leaves a blank page.
 */
export class RootErrorBoundary extends Component<{ children: ReactNode }, State> {
  override state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  override componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('The React app stopped on an error', error, info.componentStack)
  }

  override render() {
    return this.state.hasError ? <ErrorFallback /> : this.props.children
  }
}
