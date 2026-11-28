// Why: Ktor's JS client imports the Node "ws" package dynamically, and only runs that import under Node; the browser
// uses its own WebSocket. Aliasing "ws" to this keeps Vite from looking for a Node module.
export default globalThis.WebSocket
