import { MainPresenter, type JsMainTab } from 'bibleplanner-shared'

import { usePresenter, useWatch } from '@/bridge/usePresenter'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { MainTabList } from '@/app/MainTabList'

/**
 * A placeholder that proves the wiring: it builds the main screen's shared ViewModel through its presenter and lists
 * the tabs it exposes. The real screens replace it feature by feature.
 */
export function App() {
  const presenter = usePresenter(() => new MainPresenter())
  const tabs = useWatch(presenter, (p, onChange: (tabs: JsMainTab[]) => void) => p.observeTabs(onChange))

  return (
    <main className="mx-auto flex min-h-svh max-w-md flex-col justify-center px-4 py-8">
      <Card>
        <CardHeader>
          <CardTitle>Bible Planner</CardTitle>
          <CardDescription>The React web app, wired to the shared Kotlin ViewModels.</CardDescription>
        </CardHeader>
        <CardContent>{tabs ? <MainTabList tabs={tabs} /> : <p className="text-sm text-muted-foreground">Loading…</p>}</CardContent>
      </Card>
    </main>
  )
}
