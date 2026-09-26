package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * A `@Composable` that emits UI ends in a word that says what kind of UI it is, taken from a closed list,
 * so a name alone tells a screen from a row from a side effect.
 *
 * Only UI emitters are held to it: a composable that returns a value (`rememberDayProgress`, `chapterCountText`)
 * is lowercase and follows the value-returning naming rule instead, and a `@Preview` is named after what
 * it previews.
 */
class ComposableNamingSuffixRule : BiblePlannerRule("composable-naming-suffix") {
    private val allowedSuffixes = ALLOWED_SUFFIXES.split(' ')

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        val name = function.name ?: return
        if (!name.first().isUpperCase()) return
        if (!function.hasAnnotationContaining("Composable") || function.hasAnnotationContaining("Preview")) return
        if (allowedSuffixes.any { suffix -> name.endsWith(suffix) }) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        emit(
            identifier.startOffset,
            "Composable '$name' must end with one of the allowed suffixes: ${allowedSuffixes.joinToString()}. " +
                "When none describes it, use 'Component'",
            false,
        )
    }

    private fun KtNamedFunction.hasAnnotationContaining(name: String): Boolean =
        annotationEntries.any { annotation -> annotation.shortName?.asString()?.contains(name) == true }

    private companion object {
        const val ALLOWED_SUFFIXES =
            "Action Badge Banner Bar Box Bubble Button Card Cell Chip Collector Column Component Content Dialog " +
                "Display Drawer Effect Fab Field Footer Grid Handle Header Heading Hero Icon Image Indicator Item " +
                "Label Layout Line List Menu Message Option Overlay Pane Panel Picker Pill Rail Root Row Scaffold " +
                "Screen Section Sheet Sidebar Skeleton Slider Snackbar Spacer Spinner Surface Switch Text Theme " +
                "Tile Title Toggle"
    }
}
