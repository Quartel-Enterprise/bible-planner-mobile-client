package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

private const val UNIT = "Unit"

class UnitFunctionBlockBodyRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:unit-function-block-body"),
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
        if (node.elementType != FUN) return

        val function = node.psi as? KtNamedFunction ?: return
        val name = function.name ?: return
        val body = function.expressionBody() ?: return
        if (!function.returnsUnit() && !body.delegatesToUnitFunction(function)) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        emit(
            identifier.startOffset,
            "Function '$name' returns 'Unit' but uses an expression body; open a block body " +
                "({ … }) instead of assigning with '='",
            false,
        )
    }

    private fun KtNamedFunction.expressionBody(): KtExpression? = bodyExpression?.takeIf { !hasBlockBody() }

    private fun KtNamedFunction.returnsUnit(): Boolean = typeReference?.text == UNIT

    private fun KtExpression.delegatesToUnitFunction(caller: KtNamedFunction): Boolean {
        if (caller.typeReference != null) return false
        val calleeName = calleeName() ?: return false
        val candidates = caller.functionsInScope().filter { it.name == calleeName }
        return candidates.isNotEmpty() && candidates.all { it.isKnownUnit() }
    }

    private fun KtExpression.calleeName(): String? = (this as? KtCallExpression)
        ?.calleeExpression
        ?.let { it as? KtNameReferenceExpression }
        ?.getReferencedName()

    private fun KtNamedFunction.functionsInScope(): List<KtNamedFunction> {
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
