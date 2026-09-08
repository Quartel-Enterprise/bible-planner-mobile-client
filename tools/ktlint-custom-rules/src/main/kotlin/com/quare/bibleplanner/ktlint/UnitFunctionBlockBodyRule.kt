package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

private const val UNIT = "Unit"

class UnitFunctionBlockBodyRule : BiblePlannerRule("unit-function-block-body") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return

        val function = node.psi as? KtNamedFunction ?: return
        val name = function.name ?: return
        val body = function.findExpressionBody() ?: return
        if (!function.hasUnitReturnType() && !body.isDelegatingToUnitFunction(function)) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        emit(
            identifier.startOffset,
            "Function '$name' returns 'Unit' but uses an expression body; open a block body " +
                "({ … }) instead of assigning with '='",
            false,
        )
    }

    private fun KtNamedFunction.findExpressionBody(): KtExpression? = bodyExpression?.takeIf { !hasBlockBody() }

    private fun KtNamedFunction.hasUnitReturnType(): Boolean = typeReference?.text == UNIT

    private fun KtExpression.isDelegatingToUnitFunction(caller: KtNamedFunction): Boolean {
        if (caller.typeReference != null) return false
        val calleeName = findCalleeName() ?: return false
        val candidates = caller.getFunctionsInScope().filter { it.name == calleeName }
        return candidates.isNotEmpty() && candidates.all { it.isKnownUnit() }
    }

    private fun KtExpression.findCalleeName(): String? = (this as? KtCallExpression)
        ?.calleeExpression
        ?.let { it as? KtNameReferenceExpression }
        ?.getReferencedName()

    private fun KtNamedFunction.getFunctionsInScope(): List<KtNamedFunction> {
        val declarations = containingClassOrObject?.declarations ?: containingKtFile.declarations
        return (declarations + containingKtFile.declarations)
            .filterIsInstance<KtNamedFunction>()
            .distinct()
    }

    private fun KtNamedFunction.isKnownUnit(): Boolean {
        val declaredReturnType = typeReference?.text ?: return hasBlockBody()
        return declaredReturnType == UNIT
    }
}
