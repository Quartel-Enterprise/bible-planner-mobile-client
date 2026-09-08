package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtProperty

class TopLevelValBlankLineRule : BiblePlannerRule("top-level-val-blank-line") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return

        node
            .children20
            .filter { it.isWhiteSpace20 && it.text.count { char -> char == '\n' } > 1 }
            .forEach { whiteSpace ->
                val previous = whiteSpace.treePrev ?: return@forEach
                val next = whiteSpace.treeNext ?: return@forEach
                if (!previous.isTopLevelPrivateValOrConst() || !next.isTopLevelPrivateValOrConst()) return@forEach
                val identifier = next.findChildByType(IDENTIFIER) ?: return@forEach
                emit(
                    identifier.startOffset,
                    "Top-level private val/const val '${identifier.text}' should follow the previous one " +
                        "directly, without a blank line between them",
                    false,
                )
            }
    }

    private fun ASTNode.isTopLevelPrivateValOrConst(): Boolean = elementType == PROPERTY &&
        hasModifier(PRIVATE_KEYWORD) &&
        (psi as? KtProperty)?.receiverTypeReference == null
}
