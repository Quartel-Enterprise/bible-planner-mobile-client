package com.quare.bibleplanner.core.provider.analytics

import java.io.File
import kotlin.test.Test
import kotlin.test.fail

/**
 * Static, repo-wide checks that give `EventAnalytics.Track.Manual` and `EventAnalytics.Track.Automatic`
 * teeth beyond the compiler's exhaustiveness guarantee: every name a `UiEvent` declares as tracked
 * must actually be wired to a `trackEvent` call in its own module, and must have a catalog entry
 * under `docs/analytics/events/`. The catalog itself is then checked against the event index in
 * `docs/analytics/README.md`, so a documented event cannot stay invisible to anyone reading the index.
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

    @Test
    fun `every docs analytics events catalog entry is listed in the README index and vice versa`() {
        val documented = eventsDir
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "md" }
            .map { it.nameWithoutExtension }
            .toSet()
        val indexed = indexedEventNames()

        val violations = (documented - indexed).sorted().map {
            "$it: docs/analytics/events/$it.md has no row in the README event index"
        } + (indexed - documented).sorted().map {
            "$it: README event index links to docs/analytics/events/$it.md, which does not exist"
        }
        if (violations.isNotEmpty()) {
            fail("README event index out of sync with the catalog:\n" + violations.joinToString("\n"))
        }
    }

    private fun indexedEventNames(): Set<String> {
        val readme = File(repoRoot, "docs/analytics/README.md").readText()
        val heading = EVENT_INDEX_HEADING_REGEX.find(readme)
            ?: fail("Heading \"$EVENT_INDEX_HEADING\" not found in docs/analytics/README.md")
        val start = heading.range.last
        val end = SECTION_HEADING_REGEX
            .find(readme, startIndex = start)
            ?.range
            ?.first
            ?: readme.length
        return INDEX_ROW_REGEX
            .findAll(readme.substring(start, end))
            .map { it.groupValues[1] }
            .toSet()
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
        const val EVENT_INDEX_HEADING = "## Event index"
        val ANALYTICS_EVENT_NAME_REGEX = Regex("""AnalyticsEventNames\.([A-Z0-9_]+)""")
        val TRACK_NAME_REGEX = Regex("""name\s*=\s*AnalyticsEventNames\.([A-Z0-9_]+)""")
        val CONST_VAL_REGEX = Regex("""const val ([A-Z0-9_]+) = "([a-z0-9_]+)"""")
        val EVENT_INDEX_HEADING_REGEX = Regex(
            pattern = """^## Event index$""",
            option = RegexOption.MULTILINE,
        )
        val SECTION_HEADING_REGEX = Regex(
            pattern = """^## """,
            option = RegexOption.MULTILINE,
        )
        val INDEX_ROW_REGEX = Regex(
            pattern = """^\|\s*\[[a-z0-9_]+]\(events/([a-z0-9_]+)\.md\)""",
            option = RegexOption.MULTILINE,
        )
    }
}
