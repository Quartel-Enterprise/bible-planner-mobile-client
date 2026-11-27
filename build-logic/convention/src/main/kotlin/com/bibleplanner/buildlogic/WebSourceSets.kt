package com.bibleplanner.buildlogic

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Why: a module that adds its own dependsOn edges turns off the default hierarchy, so webMain no longer
 * feeds the two web targets; this puts those edges back. Only such a module calls it: an edge added
 * by hand in every module would turn the default hierarchy off everywhere.
 */
fun KotlinMultiplatformExtension.dependWebTargetsOnWebMain() {
    val webMain = sourceSets.findByName("webMain") ?: return
    listOf("jsMain", "wasmJsMain").forEach { name ->
        val targetMain = sourceSets.findByName(name) ?: return@forEach
        if (webMain !in targetMain.dependsOn) {
            targetMain.dependsOn(webMain)
        }
    }
}
