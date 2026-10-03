package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression

class SuspendRunCatchingRule : BiblePlannerRule("suspend-run-catching") {
    private val broadTypeNames = setOf("Exception", "Throwable")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != TRY) return
        val catchClauses = (node.psi as? KtTryExpression)?.catchClauses ?: return
        if (catchClauses.none { clause -> clause.isCancellationRethrow() }) return
        if (catchClauses.none { clause -> clause.findCaughtTypeName() in broadTypeNames }) return

        emit(
            node.startOffset,
            "Rethrowing CancellationException and catching everything else by hand is what suspendRunCatching " +
                "does: use it and handle the Result with onSuccess/onFailure",
            false,
        )
    }

    private fun KtCatchClause.findCaughtTypeName(): String? =
        catchParameter?.typeReference?.text?.substringAfterLast('.')

    private fun KtCatchClause.isCancellationRethrow(): Boolean {
        if (findCaughtTypeName() != CANCELLATION_EXCEPTION) return false
        val statement = (catchBody as? KtBlockExpression)?.statements?.singleOrNull() ?: return false
        val thrown = (statement as? KtThrowExpression)?.thrownExpression as? KtNameReferenceExpression ?: return false
        return thrown.getReferencedName() == catchParameter?.name
    }

    private companion object {
        const val CANCELLATION_EXCEPTION = "CancellationException"
    }
}
