package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class WhenEntrySingleStatementBracesRule : BiblePlannerRule("when-entry-single-statement-braces") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != WHEN_ENTRY) return

        val block = node.findChildByType(BLOCK) ?: return
        val statements =
            block
                .children20
                .filterNot { it.elementType == LBRACE || it.elementType == RBRACE || it.isWhiteSpace20 }
                .toList()
        val statement = statements.singleOrNull() ?: return
        if (statement.text.contains('\n')) return
        // A bare lambda literal as the sole statement is the block's return value (e.g. a branch typed
        // `() -> Unit`). The outer braces are the required block syntax and cannot be collapsed into the
        // lambda's own braces without changing meaning (eager execution instead of a deferred lambda).
        if (statement.elementType == LAMBDA_EXPRESSION) return

        emit(
            block.startOffset,
            "when entry with a single-line body should not be wrapped in braces",
            false,
        )
    }
}
