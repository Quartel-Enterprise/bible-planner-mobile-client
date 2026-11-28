import { createContext, use, type ReactNode } from 'react'

import { messages, type Locale, type Messages } from './messages'

const MessagesContext = createContext<Messages>(messages.en)

export function I18nProvider({ locale, children }: { locale: Locale; children: ReactNode }) {
  return <MessagesContext value={messages[locale]}>{children}</MessagesContext>
}

/** The strings of the locale the nearest I18nProvider gives, English without one. */
// eslint-disable-next-line react-refresh/only-export-components -- the hook belongs with its provider
export function useMessages(): Messages {
  return use(MessagesContext)
}
