package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class WhenEntrySingleStatementBracesRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:when-entry-single-statement-braces"),
        about = About(
            maintainer = "Bible Planner",
            repositoryUrl = "https://github.com/quare-tech/bible-planner-mobile-client",
            issueTrackerUrl = "https://github.com/quare-tech/bible-planner-mobile-client/issues",
        ),
    ) {
    @Deprecated("Marked for removal in Ktlint 2.0")
    @Suppress("DEPRECATION")
    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType != WHEN_ENTRY) return

        val block = node.findChildByType(BLOCK) ?: return
        val statements =
            block
                .children()
                .filterNot { it.elementType == LBRACE || it.elementType == RBRACE || it.isWhiteSpace() }
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
