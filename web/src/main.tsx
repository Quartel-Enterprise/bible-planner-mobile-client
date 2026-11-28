import { StrictMode, type ReactNode } from 'react'
import { createRoot } from 'react-dom/client'

import { App } from '@/app/App'
import { ErrorFallback, RootErrorBoundary } from '@/app/RootErrorBoundary'
import { startShared } from '@/bridge/shared'
import { I18nProvider } from '@/i18n/I18nProvider'
import { applyDocumentLocale, detectLocale } from '@/i18n/locale'
import { messages } from '@/i18n/messages'
import './index.css'

const locale = detectLocale()
applyDocumentLocale(locale)
document.title = messages[locale].appName

const container = document.getElementById('root')
if (!container) throw new Error('index.html has no #root element')
const root = createRoot(container)

const render = (node: ReactNode) => {
  root.render(
    <StrictMode>
      <I18nProvider locale={locale}>{node}</I18nProvider>
    </StrictMode>,
  )
}

try {
  startShared()
  render(
    <RootErrorBoundary>
      <App />
    </RootErrorBoundary>,
  )
} catch (error) {
  // Why: Koin failing to start happens before React renders, so the boundary can't catch it.
  console.error('The shared app failed to start', error)
  render(<ErrorFallback />)
}
