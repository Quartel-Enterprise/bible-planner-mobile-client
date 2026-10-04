package com.quare.bibleplanner.core.plan.data.sync

/*
 * Why: values match the legacy DataStore keys so the one-time migration keeps
 * continuity; do not rename them.
 */
internal object PlanPreferenceKeys {
    const val PLAN_START_DATE = "plan_start_date"
    const val SELECTED_READING_PLAN = "selected_reading_plan"
}
