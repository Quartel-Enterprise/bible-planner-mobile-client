/** The languages the app ships, as BCP 47 tags (the Android resource folders values, values-pt-rBR, values-es). */
export const locales = ['en', 'pt-BR', 'es'] as const

export type Locale = (typeof locales)[number]

/** Every user-facing string of the React app. A missing translation is a type error. */
export interface Messages {
  appName: string
  loading: string
  tabs: {
    plans: string
    books: string
    profile: string
  }
  error: {
    message: string
    retry: string
  }
}

/*
 * Why: the same translations the other UIs use: appName from androidApp's app_name, the tabs from
 * feature/main's strings.xml, loading from android/designsystem's progress_dialog_loading, and the error from
 * feature/paywall's error_unknown and feature/chat's chat_retry.
 */
export const messages: Record<Locale, Messages> = {
  en: {
    appName: 'Bible Planner',
    loading: 'Loading',
    tabs: { plans: 'Plans', books: 'Books', profile: 'Profile' },
    error: { message: 'Something went wrong. Please try again.', retry: 'Try again' },
  },
  'pt-BR': {
    appName: 'Planner Bíblico',
    loading: 'Carregando',
    tabs: { plans: 'Planos', books: 'Livros', profile: 'Perfil' },
    error: { message: 'Algo deu errado. Por favor, tente novamente.', retry: 'Tentar novamente' },
  },
  es: {
    appName: 'Planner Bíblico',
    loading: 'Cargando',
    tabs: { plans: 'Planes', books: 'Libros', profile: 'Perfil' },
    error: { message: 'Algo salió mal. Por favor, inténtalo de nuevo.', retry: 'Intentar de nuevo' },
  },
}

/**
 * Picks the first of the browser's languages the app has, matching on the language alone (pt-PT gets pt-BR, es-MX
 * gets es), and English otherwise.
 */
export function resolveLocale(languages: readonly string[]): Locale {
  for (const language of languages) {
    const base = language.toLowerCase().split('-')[0]
    const match = locales.find((locale) => locale.toLowerCase().split('-')[0] === base)
    if (match) return match
  }
  return 'en'
}
