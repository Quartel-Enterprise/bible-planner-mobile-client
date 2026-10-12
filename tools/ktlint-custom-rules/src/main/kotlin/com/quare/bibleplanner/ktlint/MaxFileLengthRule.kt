package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class MaxFileLengthRule : BiblePlannerRule("max-file-length") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return
        val lineCount = node.text.countLines()
        if (lineCount <= MAX_LINES) return
        emit(
            0,
            "File has $lineCount lines, more than the $MAX_LINES allowed: move a responsibility into a class, " +
                "mapper or composable of its own",
            false,
        )
    }

    /*
     * Why: counted the way the editor numbers lines, so the final newline doesn't add one; `wc -l`
     * agrees only when the file ends in a newline.
     */
    private fun String.countLines(): Int {
        val newlines = count { it == '\n' }
        return if (isEmpty() || endsWith('\n')) newlines else newlines + 1
    }

    private companion object {
        const val MAX_LINES = 600
    }
}
