package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class PrepareScenarioLastMemberRule : BiblePlannerRule("prepare-scenario-last-member") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS_BODY) return
        val declarations = (node.psi as? KtClassBody)?.declarations ?: return
        val prepareScenarioIndex = declarations.indexOfFirst(KtDeclaration::isPrepareScenario)
        if (prepareScenarioIndex == -1) return

        declarations
            .drop(prepareScenarioIndex + 1)
            // Why: a class declares its companion object at its end (docs/architecture/code-style.md).
            .filterNot { declaration -> declaration.isPrepareScenario() || declaration.isCompanionObject() }
            .forEach { declaration ->
                emit(
                    declaration.textOffset,
                    "'${declaration.name}' comes after '$PREPARE_SCENARIO', which is the last member of the class " +
                        "(only the companion object may follow it): move it above, or to the top level after the class",
                    false,
                )
            }
    }

    private fun KtDeclaration.isCompanionObject(): Boolean = this is KtObjectDeclaration && isCompanion()
}
