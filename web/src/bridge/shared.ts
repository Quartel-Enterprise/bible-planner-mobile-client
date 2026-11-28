import { startBiblePlanner } from 'bibleplanner-shared'

/**
 * Starts the shared Kotlin app (Koin) before any presenter is created. The Kotlin side ignores a second call, so a
 * hot reload of this module needs no flag of its own.
 */
export function startShared() {
  startBiblePlanner()
}
