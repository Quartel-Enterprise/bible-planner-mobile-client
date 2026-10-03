package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtWhenExpression

class TwoBranchWhenRule : BiblePlannerRule("two-branch-when") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != WHEN) return
        val entries = (node.psi as? KtWhenExpression)?.entries ?: return
        if (entries.size != 2 || entries.none { entry -> entry.isElse }) return

        emit(
            node.startOffset,
            "A when with a single branch besides else is an if/else: keep when for three or more branches",
            false,
        )
    }
}
