package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty

class FunInterfaceRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:fun-interface-required"),
        about = About(
            maintainer = "Bible Planner",
            repositoryUrl = "https://github.com/quare-tech/bible-planner-mobile-client",
            issueTrackerUrl = "https://github.com/quare-tech/bible-planner-mobile-client/issues",
        ),
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS) return
        val ktClass = node.psi as? KtClass ?: return
        if (!ktClass.isInterface() || node.hasModifier(FUN_KEYWORD)) return

        val classBody = node.findChildByType(CLASS_BODY) ?: return
        val abstractFunctions = classBody
            .children()
            .filter { it.elementType == FUN && it.isAbstractFunction() }
            .toList()
        val hasAbstractProperty = classBody
            .children()
            .any { it.elementType == PROPERTY && it.isAbstractProperty() }

        if (abstractFunctions.size == 1 && !hasAbstractProperty) {
            val identifier = node.findChildByType(IDENTIFIER) ?: return
            emit(
                identifier.startOffset,
                "Interface '${identifier.text}' has a single abstract method and should be declared as 'fun interface'",
                false,
            )
        }
    }

    private fun ASTNode.isAbstractFunction(): Boolean {
        val function = psi as? KtFunction ?: return false
        return !function.hasBody() && function.valueParameters.none { it.hasDefaultValue() }
    }

    private fun ASTNode.isAbstractProperty(): Boolean {
        val property = psi as? KtProperty ?: return false
        return property.initializer == null &&
            property.delegate == null &&
            property.getter?.hasBody() != true
    }
}
