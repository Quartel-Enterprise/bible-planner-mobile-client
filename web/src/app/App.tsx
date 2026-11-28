import { MainPresenter, type JsMainTab } from 'bibleplanner-shared'

import { usePresenter, useWatch } from '@/bridge/usePresenter'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { MainTabList } from '@/app/MainTabList'
import { useMessages } from '@/i18n/I18nProvider'

/**
 * A placeholder that proves the wiring: it builds the main screen's shared ViewModel through its presenter and lists
 * the tabs it exposes. The real screens replace it feature by feature.
 */
export function App() {
  const strings = useMessages()
  const presenter = usePresenter(() => new MainPresenter())
  const tabs = useWatch(presenter, (p, onChange: (tabs: JsMainTab[]) => void) => p.observeTabs(onChange))

  return (
    <main className="mx-auto flex min-h-svh max-w-md flex-col justify-center px-4 py-8">
      <Card>
        <CardHeader>
          <CardTitle>{strings.appName}</CardTitle>
        </CardHeader>
        <CardContent>
          {tabs ? <MainTabList tabs={tabs} /> : <p className="text-sm text-muted-foreground">{strings.loading}</p>}
        </CardContent>
      </Card>
    </main>
  )
}
