import { BookOpen, CalendarDays, CircleUser } from 'lucide-react'

import { Button } from '@/components/ui/button'

/** The shape of the shared library's JsMainTab that the list reads. */
export interface MainTab {
  readonly key: string
  readonly profileName: string | null | undefined
}

const knownTabs: Record<string, { label: string; Icon: typeof CircleUser }> = {
  plans: { label: 'Plans', Icon: CalendarDays },
  books: { label: 'Books', Icon: BookOpen },
  profile: { label: 'Profile', Icon: CircleUser },
}

/** A blank display name (a user without one) falls back to the tab's own label instead of an empty button. */
function getTabLabel(tab: MainTab): string {
  const name = tab.profileName?.trim()
  if (name) return name
  return knownTabs[tab.key]?.label ?? tab.key
}

export function MainTabList({ tabs }: { tabs: readonly MainTab[] }) {
  return (
    <ul className="flex flex-col gap-2">
      {tabs.map((tab) => {
        const Icon = knownTabs[tab.key]?.Icon ?? CircleUser
        return (
          <li key={tab.key}>
            <Button variant="secondary" className="w-full justify-start">
              <Icon aria-hidden />
              {getTabLabel(tab)}
            </Button>
          </li>
        )
      })}
    </ul>
  )
}
