package com.quare.bibleplanner.core.provider.analytics.domain.model

/**
 * The analytics decision carried by a user-intent event. Every `UiEvent` declares one, so a new
 * event cannot compile without a conscious decision about how it is tracked — the same compile-time
 * guarantee the destination mapper gives for `destination_view`. The top-level split answers
 * whether the event is tracked at all ([Track]) or not ([NotTracked]); [Track] then splits on *how*.
 */
sealed interface EventAnalytics {
    /**
     * The event is tracked. The base `TrackedViewModel` auto-emits [Automatic]; [Manual] is a
     * record of intent that an automated test cross-checks against the actual tracking calls and
     * the catalog.
     */
    sealed interface Track : EventAnalytics {
        /**
         * Tracked automatically with [name] and [params]. Use only when the event name and every
         * parameter are known from the event itself (its own fields or compile-time constants); do
         * not also emit it manually. [params] is empty when the event has no parameters.
         */
        data class Automatic(
            val name: String,
            val params: Map<String, Any>,
        ) : Track

        /**
         * Tracked by an explicit `trackEvent` call inside the event handler rather than
         * automatically, because a parameter is only known after a domain call or from the current
         * UI state. [names] lists every event the handler may emit; an automated test verifies each
         * one is actually wired to a `trackEvent` call in the same module.
         */
        data class Manual(
            val names: Set<String>,
        ) : Track {
            constructor(name: String) : this(setOf(name))
        }
    }

    /**
     * Genuinely not tracked — UI plumbing that is not a user action (scroll, menu open/close,
     * animation callbacks, retry). The conscious opt-out; no business action is ever left here.
     */
    data object NotTracked : EventAnalytics
}
