package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtProperty

private val topLevelDeclarationElementTypes = setOf(FUN, CLASS, OBJECT_DECLARATION)

class TopLevelValPositionRule : BiblePlannerRule("top-level-val-position") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return

        var seenTopLevelDeclaration = false
        node.children20.forEach { child ->
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
