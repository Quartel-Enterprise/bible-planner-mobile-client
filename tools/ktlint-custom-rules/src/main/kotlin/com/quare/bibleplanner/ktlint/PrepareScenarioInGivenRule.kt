package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class PrepareScenarioInGivenRule : BiblePlannerRule("prepare-scenario-in-given") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        if (!function.isTest()) return
        val sections = function.findTestSections()

        function
            .collectDescendantsOfType<KtCallExpression> { call -> call.calleeExpression?.text == PREPARE_SCENARIO }
            .forEach { call ->
                val section = sections.lastOrNull { (_, offset) -> offset < call.textOffset }?.first
                if (section == TestSection.GIVEN) return@forEach
                val placement = section?.let { "under '${it.comment}'" } ?: "before any section"
                emit(
                    call.textOffset,
                    "'$PREPARE_SCENARIO' builds the scenario, so it is called under '${TestSection.GIVEN.comment}', " +
                        "not $placement",
                    false,
                )
            }
    }
}
