// OPFS, where the SQLite worker keeps the Room database, needs a cross-origin isolated page.
;(function (config) {
    config.devServer = config.devServer || {}
    config.devServer.headers = [
        { key: 'Cross-Origin-Opener-Policy', value: 'same-origin' },
        { key: 'Cross-Origin-Embedder-Policy', value: 'require-corp' },
    ]
})(config)
