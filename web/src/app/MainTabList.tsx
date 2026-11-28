import { BookOpen, CalendarDays, CircleUser } from 'lucide-react'

import { Button } from '@/components/ui/button'

/** The shape of the shared library's JsMainTab that the list reads. */
export interface MainTab {
  readonly key: string
  readonly profileName: string | null | undefined
}

const tabLabels: Record<string, string> = {
  plans: 'Plans',
  books: 'Books',
  profile: 'Profile',
}

function TabIcon({ tabKey }: { tabKey: string }) {
  switch (tabKey) {
    case 'plans':
      return <CalendarDays aria-hidden />
    case 'books':
      return <BookOpen aria-hidden />
    default:
      return <CircleUser aria-hidden />
  }
}

export function MainTabList({ tabs }: { tabs: readonly MainTab[] }) {
  return (
    <ul className="flex flex-col gap-2">
      {tabs.map((tab) => (
        <li key={tab.key}>
          <Button variant="secondary" className="w-full justify-start">
            <TabIcon tabKey={tab.key} />
            {tab.profileName ?? tabLabels[tab.key] ?? tab.key}
          </Button>
        </li>
      ))}
    </ul>
  )
}
