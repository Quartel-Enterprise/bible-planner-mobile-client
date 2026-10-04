package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import com.pinterest.ktlint.rule.engine.core.api.nextLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiCommentImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

class CommentsSayWhyRule : BiblePlannerRule("comments-say-why") {
    private val commentTypes = setOf(EOL_COMMENT, BLOCK_COMMENT, KDOC)

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType !in commentTypes) return
        // Why: the autocorrect of a multi-line '// Why:' detaches its continuation lines, which ktlint still visits.
        if (node.isDetachedFromFile()) return

        when {
            node.isWhyLineComment() -> checkWhyLineComment(
                head = node,
                emit = emit,
            )

            node.isWhyBlockComment() -> checkWhyBlockComment(
                node = node,
                emit = emit,
            )

            node.isWhyContinuation() -> return

            else -> emit(
                node.startOffset,
                "Production code only keeps '$WHY_LINE_PREFIX' comments (a '/* */' block when they run over several " +
                    "lines), which say why the code is the way it is: say what it does with names instead",
                false,
            )
        }
    }

    private fun checkWhyLineComment(
        head: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val continuations = generateSequence(
            head.findCommentOnNextLine(),
        ) { comment -> comment.findCommentOnNextLine() }.toList()
        if (continuations.isEmpty()) return
        val indent = head.findLineIndent()
        val lines = (listOf(head) + continuations).map { comment -> comment.toLineCommentContent() }
        // Why: Kotlin block comments nest, so a '/*' or '*/' inside the text would break the converted comment.
        val canBeAutoCorrected = indent != null && lines.none { line -> "/*" in line || "*/" in line }

        emit(head.startOffset, MULTI_LINE_MESSAGE, canBeAutoCorrected).ifAutocorrectAllowed {
            continuations.forEach { continuation ->
                continuation.prevLeaf?.let { whiteSpace -> whiteSpace.treeParent.removeChild(whiteSpace) }
                continuation.treeParent.removeChild(continuation)
            }
            head.replaceWithComment(
                type = BLOCK_COMMENT,
                text = buildBlockComment(
                    lines = lines,
                    indent = indent.orEmpty(),
                ),
            )
        }
    }

    private fun checkWhyBlockComment(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val lines = node.toBlockCommentContent()
        val indent = node.findLineIndent()

        if (lines.size == 1) {
            // Why: a '//' comment runs to the end of the line, so it would swallow any code after the block.
            emit(node.startOffset, SINGLE_LINE_MESSAGE, node.isLastOnLine()).ifAutocorrectAllowed {
                node.replaceWithComment(
                    type = EOL_COMMENT,
                    text = "// ${lines.single()}",
                )
            }
            return
        }

        val expectedText = buildBlockComment(
            lines = lines,
            indent = indent.orEmpty(),
        )
        if (indent != null && node.text == expectedText) return

        emit(node.startOffset, MULTI_LINE_MESSAGE, indent != null).ifAutocorrectAllowed {
            node.replaceWithComment(
                type = BLOCK_COMMENT,
                text = expectedText,
            )
        }
    }

    private fun ASTNode.isDetachedFromFile(): Boolean =
        generateSequence(this, ASTNode::getTreeParent).last().elementType != FILE

    private fun ASTNode.isWhyLineComment(): Boolean = elementType == EOL_COMMENT && text.startsWith(WHY_LINE_PREFIX)

    private fun ASTNode.isWhyBlockComment(): Boolean =
        elementType == BLOCK_COMMENT && toBlockCommentContent().firstOrNull()?.startsWith(WHY_LABEL) == true

    private fun ASTNode.isWhyContinuation(): Boolean {
        if (elementType != EOL_COMMENT) return false
        val previousComment = findCommentOnPreviousLine() ?: return false
        return previousComment.isWhyLineComment() || previousComment.isWhyContinuation()
    }

    private fun ASTNode.findCommentOnPreviousLine(): ASTNode? {
        val whiteSpace = prevLeaf?.takeIf { leaf -> leaf.isSingleLineBreak() } ?: return null
        return whiteSpace.prevLeaf?.takeIf { leaf -> leaf.elementType == EOL_COMMENT }
    }

    private fun ASTNode.findCommentOnNextLine(): ASTNode? {
        val whiteSpace = nextLeaf?.takeIf { leaf -> leaf.isSingleLineBreak() } ?: return null
        return whiteSpace.nextLeaf?.takeIf { leaf -> leaf.elementType == EOL_COMMENT && !leaf.isWhyLineComment() }
    }

    private fun ASTNode.isSingleLineBreak(): Boolean = isWhiteSpace20 && text.count { char -> char == '\n' } == 1

    private fun ASTNode.findLineIndent(): String? {
        val previous = prevLeaf ?: return ""
        if (!previous.isWhiteSpace20 || '\n' !in previous.text) return null
        return previous.text.substringAfterLast('\n')
    }

    private fun ASTNode.isLastOnLine(): Boolean {
        val next = nextLeaf ?: return true
        return next.isWhiteSpace20 && '\n' in next.text
    }

    private fun ASTNode.toLineCommentContent(): String = text.removePrefix("//").removePrefix(" ").trimEnd()

    private fun ASTNode.toBlockCommentContent(): List<String> {
        val rawLines = text.removePrefix("/*").removeSuffix("*/").lines()
        val unstarredLines = rawLines.drop(1).filter { line -> line.isNotBlank() && !line.isStarred() }
        val commonIndent = unstarredLines.minOfOrNull { line -> line.length - line.trimStart().length } ?: 0
        val firstLine = rawLines.first().trim()
        val otherLines = rawLines.drop(1).map { line ->
            if (line.isStarred()) {
                line
                    .trimStart()
                    .removePrefix("*")
                    .removePrefix(" ")
                    .trimEnd()
            } else {
                line.drop(commonIndent).trimEnd()
            }
        }
        return (listOf(firstLine) + otherLines)
            .dropWhile(String::isBlank)
            .dropLastWhile(String::isBlank)
    }

    private fun String.isStarred(): Boolean = trimStart().startsWith("*")

    private fun buildBlockComment(
        lines: List<String>,
        indent: String,
    ): String = buildString {
        appendLine("/*")
        lines.forEach { line ->
            append("$indent *")
            if (line.isNotEmpty()) append(" $line")
            appendLine()
        }
        append("$indent */")
    }

    private fun ASTNode.replaceWithComment(
        type: IElementType,
        text: String,
    ) {
        treeParent.replaceChild(this, PsiCommentImpl(type, text))
    }

    private companion object {
        const val WHY_LABEL = "Why:"
        const val WHY_LINE_PREFIX = "// $WHY_LABEL"
        const val MULTI_LINE_MESSAGE =
            "A '// Why:' that runs over several lines is a block comment: '/*' on its own line, ' * ' before " +
                "each line, and ' */' on its own line"
        const val SINGLE_LINE_MESSAGE = "A Why comment that fits on one line is a '// Why:' comment, not a block"
    }
}
