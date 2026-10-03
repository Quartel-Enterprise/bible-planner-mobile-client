package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class CommentsSayWhyRule : BiblePlannerRule("comments-say-why") {
    private val commentTypes = setOf(EOL_COMMENT, BLOCK_COMMENT, KDOC)

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType !in commentTypes) return
        if (node.isWhyComment()) return

        emit(
            node.startOffset,
            "Production code only keeps '$WHY_PREFIX' comments, which say why the code is the way it is: " +
                "say what it does with names instead",
            false,
        )
    }

    private fun ASTNode.isWhyComment(): Boolean {
        if (elementType != EOL_COMMENT) return false
        if (text.startsWith(WHY_PREFIX)) return true
        val previousComment = findCommentOnPreviousLine() ?: return false
        return previousComment.isWhyComment()
    }

    private fun ASTNode.findCommentOnPreviousLine(): ASTNode? {
        val whiteSpace = prevLeaf?.takeIf { leaf -> leaf.isWhiteSpace20 } ?: return null
        if (whiteSpace.text.count { char -> char == '\n' } != 1) return null
        return whiteSpace.prevLeaf?.takeIf { leaf -> leaf.elementType == EOL_COMMENT }
    }

    private companion object {
        const val WHY_PREFIX = "// Why:"
    }
}
