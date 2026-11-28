import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import type { Plugin } from 'vite'
import { defineConfig } from 'vitest/config'

const sqliteWorkerSpecifier = 'bibleplanner-sqlite-worker/worker.js'

/*
 * Why: :core:provider:room starts its SQLite worker with new URL("bibleplanner-sqlite-worker/worker.js",
 * import.meta.url), a package webpack resolves for the Compose web app. Vite reads that URL as a file next to the
 * library, which isn't there (the library's package.json leaves local npm packages out), so this points it at the
 * worker's source in the module.
 */
function sqliteWorker(): Plugin {
  return {
    name: 'bibleplanner-sqlite-worker',
    resolveId(source) {
      return source.endsWith(sqliteWorkerSpecifier)
        ? path.resolve(import.meta.dirname, '../core/provider/room/web-worker/worker.js')
        : null
    },
  }
}

export default defineConfig({
  plugins: [react(), tailwindcss(), sqliteWorker()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
      ws: path.resolve(import.meta.dirname, './src/bridge/ws-browser.ts'),
    },
    // Why: the shared library and the worker live outside web/, so their imports resolve from web/node_modules.
    dedupe: ['@js-joda/core', '@sqlite.org/sqlite-wasm'],
  },
  // Why: both resolve files next to themselves with import.meta.url (the worker, SQLite's wasm), which pre-bundling
  // would move.
  optimizeDeps: {
    exclude: ['bibleplanner-shared', '@sqlite.org/sqlite-wasm'],
  },
  worker: {
    format: 'es',
    plugins: () => [sqliteWorker()],
  },
  test: {
    environment: 'jsdom',
    // Why: the hooks and components are tested with fakes; the Kotlin library is exercised by the app itself.
    include: ['src/**/*.test.{ts,tsx}'],
    setupFiles: ['./src/test/setup.ts'],
  },
})
