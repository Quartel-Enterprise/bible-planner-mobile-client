package com.quare.bibleplanner.core.provider.analytics

import java.io.File
import kotlin.test.Test
import kotlin.test.fail

/**
 * Static, repo-wide checks that give `EventAnalytics.Track.Manual` and `EventAnalytics.Track.Automatic`
 * teeth beyond the compiler's exhaustiveness guarantee: every name a `UiEvent` declares as tracked
 * must actually be wired to a `trackEvent` call in its own module, and must have a catalog entry
 * under `docs/analytics/events/`.
 *
 * Scope: only names declared through `analytics = EventAnalytics.Track.Automatic/Manual(...)` are
 * checked. Secondary events fired manually alongside an already-`Automatic`ed case (e.g. a paywall
 * view nested inside a menu-click branch) are outside this mechanism by design — the spec only asks
 * the *primary* decision per case to be declared — so they are not enumerated here.
 */
class AnalyticsCatalogTest {
    private val repoRoot: File = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private val eventsDir = File(repoRoot, "docs/analytics/events")

    @Test
    fun `every Track Manual name is wired to a trackEvent call elsewhere in its module`() {
        val violations = uiEventDeclarationFiles().flatMap { file ->
            val moduleRoot = file.moduleRoot()
            val text = file.readText()
            trackedManuallyNames(text).mapNotNull { name ->
                val wired = moduleRoot
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && it != file && !it.isExcluded() }
                    .any { it.readText().contains("AnalyticsEventNames.$name") }
                if (wired) {
                    null
                } else {
                    "${file.relativeTo(
                        repoRoot,
                    )}: Track.Manual($name) has no trackEvent call anywhere else in ${moduleRoot.name}"
                }
            }
        }
        if (violations.isNotEmpty()) {
            fail("Untraceable Track.Manual events:\n" + violations.joinToString("\n"))
        }
    }

    @Test
    fun `every declared Track Automatic and Track Manual name has a docs analytics events catalog entry`() {
        val nameToValue = parseAnalyticsEventNames()
        val declaredNames = uiEventDeclarationFiles()
            .flatMap { file -> trackNames(file.readText()) + trackedManuallyNames(file.readText()) }
            .toSortedSet()

        val violations = declaredNames.mapNotNull { constName ->
            val value = nameToValue[constName]
                ?: return@mapNotNull "$constName is not declared in AnalyticsEventNames.kt"
            val doc = File(eventsDir, "$value.md")
            if (doc.exists()) null else "$constName -> $value: missing docs/analytics/events/$value.md"
        }
        if (violations.isNotEmpty()) {
            fail("Uncataloged analytics events:\n" + violations.joinToString("\n"))
        }
    }

    private fun uiEventDeclarationFiles(): List<File> = File(repoRoot, "feature")
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" && !it.isExcluded() }
        .filter { it.readText().contains("EventAnalytics.") }
        .toList()

    private fun File.isExcluded(): Boolean = path.contains("${File.separator}build${File.separator}") ||
        path.contains("${File.separator}.claude${File.separator}")

    private fun File.moduleRoot(): File = generateSequence(parentFile) { it.parentFile }
        .first { File(it, "build.gradle.kts").exists() }

    private fun trackedManuallyNames(text: String): Set<String> = callArgs(text, "EventAnalytics.Track.Manual(")
        .flatMap { args -> ANALYTICS_EVENT_NAME_REGEX.findAll(args).map { it.groupValues[1] } }
        .toSet()

    private fun trackNames(text: String): Set<String> = callArgs(text, "EventAnalytics.Track.Automatic(")
        .mapNotNull { args -> TRACK_NAME_REGEX.find(args)?.groupValues?.get(1) }
        .toSet()

    private fun callArgs(
        text: String,
        marker: String,
    ): List<String> {
        val results = mutableListOf<String>()
        var idx = text.indexOf(marker)
        while (idx != -1) {
            val openIdx = idx + marker.length - 1
            var depth = 1
            var i = openIdx + 1
            while (i < text.length && depth > 0) {
                when (text[i]) {
                    '(' -> depth++
                    ')' -> depth--
                }
                i++
            }
            results += text.substring(openIdx + 1, i - 1)
            idx = text.indexOf(marker, i)
        }
        return results
    }

    private fun parseAnalyticsEventNames(): Map<String, String> {
        val file = repoRoot
            .resolve("core/provider/analytics/src/commonMain/kotlin/com/quare/bibleplanner")
            .resolve("core/provider/analytics/domain/model/AnalyticsEventNames.kt")
        return CONST_VAL_REGEX
            .findAll(file.readText())
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private companion object {
        val ANALYTICS_EVENT_NAME_REGEX = Regex("""AnalyticsEventNames\.([A-Z0-9_]+)""")
        val TRACK_NAME_REGEX = Regex("""name\s*=\s*AnalyticsEventNames\.([A-Z0-9_]+)""")
        val CONST_VAL_REGEX = Regex("""const val ([A-Z0-9_]+) = "([a-z0-9_]+)"""")
    }
}
