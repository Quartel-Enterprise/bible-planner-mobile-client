import { startBiblePlanner } from 'bibleplanner-shared'

let started = false

/** Starts the shared Kotlin app (Koin) once, before any presenter is created. */
export function startShared() {
  if (started) return
  startBiblePlanner()
  started = true
}
