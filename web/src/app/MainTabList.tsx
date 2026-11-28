import { BookOpen, CalendarDays, CircleUser } from 'lucide-react'

import { useMessages } from '@/i18n/I18nProvider'
import type { Messages } from '@/i18n/messages'

/** The shape of the shared library's JsMainTab that the list reads. */
export interface MainTab {
  readonly key: string
  readonly profileName: string | null | undefined
}

type TabKey = keyof Messages['tabs']

const tabIcons: Record<TabKey, typeof CircleUser> = {
  plans: CalendarDays,
  books: BookOpen,
  profile: CircleUser,
}

function isTabKey(key: string): key is TabKey {
  return key in tabIcons
}

/** A blank display name (a user without one) falls back to the tab's own label instead of an empty item. */
function getTabLabel(tab: MainTab, labels: Messages['tabs']): string {
  const name = tab.profileName?.trim()
  if (name) return name
  return isTabKey(tab.key) ? labels[tab.key] : tab.key
}

/** Lists the tabs as plain items: they become links once the web has navigation. */
export function MainTabList({ tabs }: { tabs: readonly MainTab[] }) {
  const { tabs: labels } = useMessages()
  return (
    <ul className="flex flex-col gap-2">
      {tabs.map((tab) => {
        const Icon = isTabKey(tab.key) ? tabIcons[tab.key] : CircleUser
        return (
          <li
            key={tab.key}
            className="flex h-9 items-center gap-2 rounded-md bg-secondary px-3 text-sm font-medium text-secondary-foreground [&_svg]:size-4"
          >
            <Icon aria-hidden />
            {getTabLabel(tab, labels)}
          </li>
        )
      })}
    </ul>
  )
}
