package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtImportDirective

class InjectedRealtimeRule : BiblePlannerRule("injected-realtime") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != IMPORT_DIRECTIVE) return
        val importPath = (node.psi as? KtImportDirective)?.importPath?.pathStr ?: return
        if (importPath != CLIENT_REALTIME_IMPORT) return

        emit(
            node.startOffset,
            "SupabaseClient.realtime is the raw plugin, which removes channels without the serialization " +
                "SerializedRemovalRealtime adds: inject Realtime instead",
            false,
        )
    }

    private companion object {
        const val CLIENT_REALTIME_IMPORT = "io.github.jan.supabase.realtime.realtime"
    }
}
