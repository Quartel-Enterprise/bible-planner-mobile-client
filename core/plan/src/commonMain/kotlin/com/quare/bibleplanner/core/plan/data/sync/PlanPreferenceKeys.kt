package com.quare.bibleplanner.core.plan.data.sync

/**
 * Keys under which reading-plan scalar preferences are stored in the synced key-value store. The
 * string values match the legacy DataStore keys so the one-time migration keeps continuity.
 */
internal object PlanPreferenceKeys {
    const val PLAN_START_DATE = "plan_start_date"
    const val SELECTED_READING_PLAN = "selected_reading_plan"
}
