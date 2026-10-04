package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction

class PrepareScenarioReturnsUnitRule : BiblePlannerRule("prepare-scenario-returns-unit") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        if (!function.isPrepareScenario()) return
        if (function.typeReference == null && function.hasBlockBody()) return
        val identifier = function.nameIdentifier ?: return

        emit(
            identifier.textOffset,
            "'$PREPARE_SCENARIO' returns nothing: give it a block body without a return type, and assign what the " +
                "tests use to lateinit var properties of the class",
            false,
        )
    }
}
