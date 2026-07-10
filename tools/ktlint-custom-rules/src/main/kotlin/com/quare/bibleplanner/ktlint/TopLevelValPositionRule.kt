package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtProperty

private val topLevelDeclarationElementTypes = setOf(FUN, CLASS, OBJECT_DECLARATION)

class TopLevelValPositionRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:top-level-val-position"),
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

        var seenTopLevelDeclaration = false
        node.children().forEach { child ->
            if (child.elementType in topLevelDeclarationElementTypes) {
                seenTopLevelDeclaration = true
            } else if (seenTopLevelDeclaration && child.isTopLevelPrivateValOrConst()) {
                val identifier = child.findChildByType(IDENTIFIER) ?: return@forEach
                emit(
                    identifier.startOffset,
                    "Top-level private val/const val '${identifier.text}' should be declared before the first " +
                        "top-level function/class/object in the file, not after",
                    false,
                )
            }
        }
    }

    private fun ASTNode.isTopLevelPrivateValOrConst(): Boolean = elementType == PROPERTY &&
        hasModifier(PRIVATE_KEYWORD) &&
        (psi as? KtProperty)?.receiverTypeReference == null
}
