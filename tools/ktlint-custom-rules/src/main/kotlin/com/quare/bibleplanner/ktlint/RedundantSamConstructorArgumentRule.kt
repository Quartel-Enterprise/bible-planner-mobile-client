package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.recursiveChildren
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgument

class RedundantSamConstructorArgumentRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:redundant-sam-constructor-argument"),
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
        if (node.elementType != FILE) return

        val funInterfaceNames = node
            .recursiveChildren()
            .filter { it.elementType == CLASS }
            .filter { (it.psi as? KtClass)?.isInterface() == true && it.hasModifier(FUN_KEYWORD) }
            .mapNotNull { it.findChildByType(IDENTIFIER)?.text }
            .toSet()
        if (funInterfaceNames.isEmpty()) return

        node
            .recursiveChildren()
            .filter { it.elementType == CALL_EXPRESSION }
            .filter { it.isRedundantSamConstructorArgument(funInterfaceNames) }
            .forEach { call ->
                emit(
                    call.startOffset,
                    "Redundant SAM constructor as an argument — pass the lambda directly ({ ... }) instead of " +
                        "wrapping it in the fun interface name",
                    false,
                )
            }
    }

    private fun ASTNode.isRedundantSamConstructorArgument(funInterfaceNames: Set<String>): Boolean {
        val call = psi as? KtCallExpression ?: return false
        val calleeName = (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
        if (calleeName !in funInterfaceNames) return false
        if (call.valueArguments.isNotEmpty()) return false
        if (call.lambdaArguments.size != 1) return false
        return call.parent is KtValueArgument
    }
}
