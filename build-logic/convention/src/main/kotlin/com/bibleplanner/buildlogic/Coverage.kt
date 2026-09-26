package com.bibleplanner.buildlogic

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

// The Kover variant CI reports and verifies. It holds only the jvm target, where commonTest and
// jvmTest run, so the merged report runs each module's tests once instead of per Android variant.
const val COVERAGE_VARIANT = "ci"

private const val TESTED_TARGET = "jvm"

// :ui:* is composables plus theme constants, which the Compose UI tests cover, so it stays out.
private val Project.isMeasuredForCoverage: Boolean
    get() = !path.startsWith(":ui:")

fun Project.configureCoverage() {
    if (!isMeasuredForCoverage) return
    pluginManager.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<KoverProjectExtension> {
        currentProject {
            createVariant(COVERAGE_VARIANT) {
                add(TESTED_TARGET)
            }
        }
    }
}
