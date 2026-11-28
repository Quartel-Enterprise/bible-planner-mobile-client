import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { searchForWorkspaceRoot, type Plugin } from 'vite'
import { defineConfig } from 'vitest/config'

const sqliteWorkerSpecifier = 'bibleplanner-sqlite-worker/worker.js'
const sqliteWorkerDirectory = path.resolve(import.meta.dirname, '../core/provider/room/web-worker')
const sharedLibraryDirectory = path.resolve(import.meta.dirname, '../shared/build/dist/js/productionLibrary')

/*
 * Why: :core:provider:room starts its SQLite worker with new URL("bibleplanner-sqlite-worker/worker.js",
 * import.meta.url), a package webpack resolves for the Compose web app. Vite reads that URL as a file next to the
 * library, which isn't there (the library's package.json leaves local npm packages out), so this points it at the
 * worker's source in the module.
 */
function sqliteWorker(): Plugin {
  const worker = path.join(sqliteWorkerDirectory, 'worker.js')
  // Why: Vite asks for the worker as worker.js?worker_file&type=module, a query it still needs, so it is kept.
  const redirect = (id: string): string | null => {
    const queryStart = id.indexOf('?')
    const file = queryStart === -1 ? id : id.slice(0, queryStart)
    const query = queryStart === -1 ? '' : id.slice(queryStart)
    return file.endsWith(sqliteWorkerSpecifier) ? worker + query : null
  }
  return {
    name: 'bibleplanner-sqlite-worker',
    // The production build resolves the worker entry through the plugins.
    resolveId: redirect,
    // The dev server rewrites the URL to /@fs/<library>/bibleplanner-sqlite-worker/worker.js and serves it as a file,
    // without asking the plugins, so the request is pointed at the worker's source before Vite looks it up.
    configureServer(server) {
      server.middlewares.use((request, _response, next) => {
        const target = request.url === undefined ? null : redirect(request.url)
        if (target !== null) request.url = `/@fs${target}`
        next()
      })
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
  // Why: the dev server only serves files under web/ by default, and the library and the worker live outside it.
  server: {
    fs: {
      allow: [searchForWorkspaceRoot(process.cwd()), sharedLibraryDirectory, sqliteWorkerDirectory],
    },
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
