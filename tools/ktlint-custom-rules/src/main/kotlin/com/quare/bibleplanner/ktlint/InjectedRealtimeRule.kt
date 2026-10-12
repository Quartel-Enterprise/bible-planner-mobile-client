package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtImportDirective

class InjectedRealtimeRule : BiblePlannerRule("injected-realtime") {
    private val pluginLookups = setOf("getPlugin", "getPluginOrNull")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val isRawRealtime = when (node.elementType) {
            IMPORT_DIRECTIVE -> (node.psi as? KtImportDirective)?.importPath?.pathStr == CLIENT_REALTIME_IMPORT
            CALL_EXPRESSION -> (node.psi as? KtCallExpression)?.isRealtimePluginLookup() == true
            else -> false
        }
        if (!isRawRealtime) return

        emit(
            node.startOffset,
            "SupabaseClient.realtime is the raw plugin, which removes channels without the serialization " +
                "SerializedRemovalRealtime adds: inject Realtime instead",
            false,
        )
    }

    private fun KtCallExpression.isRealtimePluginLookup(): Boolean = calleeExpression?.text in pluginLookups &&
        valueArguments
            .singleOrNull()
            ?.getArgumentExpression()
            ?.text
            ?.substringAfterLast('.') == REALTIME

    private companion object {
        const val CLIENT_REALTIME_IMPORT = "io.github.jan.supabase.realtime.realtime"
        const val REALTIME = "Realtime"
    }
}
