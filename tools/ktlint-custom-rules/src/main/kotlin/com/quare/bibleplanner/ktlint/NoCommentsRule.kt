package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NoCommentsRule : BiblePlannerRule("no-comments") {
    private val commentTypes = setOf(EOL_COMMENT, BLOCK_COMMENT, KDOC)

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType !in commentTypes) return

        emit(
            node.startOffset,
            "Comments are not allowed in production code: let names say what the code means",
            false,
        )
    }
}
