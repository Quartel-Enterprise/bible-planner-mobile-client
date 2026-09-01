package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ACTUAL_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OPERATOR_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

private const val ALLOWED_VERB_PREFIXES =
    "add apply are as await bind build calculate call can cancel check clamp clear close collect " +
        "compare compute configure consume contains convert copy count create crop decode decrypt " +
        "delete derive detect did disable dismiss does download drop emit enable encode encrypt " +
        "ensure equals execute extract fetch filter find flip format from generate get handle has " +
        "hash hide highlight init initialize insert install invoke is join launch load log make map " +
        "matches merge must navigate needs normalize observe of on open or orient parse pick pop " +
        "prepare produce provide read receive refresh register reload remember remove render replace " +
        "report request require reset resolve respond restore retry rotate run sanitize save search " +
        "select send should show shows sign skip sort split start stop store stream suspend sync " +
        "take throttle to toggle track transform trim unbind unregister update upload upsert use validate " +
        "verify was were will with wrap write"
private val allowedVerbPrefixes = ALLOWED_VERB_PREFIXES.split(' ').toSet()
private val ignoredReturnTypes = setOf("Unit", "Nothing")
private val exemptContainerAnnotations = setOf("Dao", "Database")
private val idiomaticSuffixes = listOf("OrNull", "Of", "For")

class ValueReturningFunctionNamingRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:value-returning-function-naming"),
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
        if (node.elementType != FUN) return
        if (node.hasModifier(OVERRIDE_KEYWORD)) return
        if (node.hasModifier(ACTUAL_KEYWORD)) return
        if (node.hasModifier(OPERATOR_KEYWORD)) return

        val function = node.psi as? KtNamedFunction ?: return
        val name = function.name ?: return
        if (!name.first().isLowerCase()) return

        val returnType = function.typeReference?.text ?: return
        if (returnType in ignoredReturnTypes) return
        if (function.isComposable() || function.isModifierExtension() || function.isInExemptContainer()) return
        if (name.isIdiomatic()) return
        if (name.takeLeadingWord() in allowedVerbPrefixes) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        val hint = if (function.valueParameters.isEmpty()) {
            "declare it as a val instead, or rename it"
        } else {
            "rename it"
        }
        emit(
            identifier.startOffset,
            "Function '$name' returns '$returnType' but is named as a noun; $hint to a verb phrase " +
                "(get/fetch/load/observe/create/build/find/to/is…)",
            false,
        )
    }

    private fun KtNamedFunction.isComposable(): Boolean =
        annotationEntries.any { it.shortName?.asString() == "Composable" }

    private fun KtNamedFunction.isModifierExtension(): Boolean =
        receiverTypeReference?.text?.substringBefore('<') == "Modifier"

    private fun KtNamedFunction.isInExemptContainer(): Boolean = containingClassOrObject
        ?.annotationEntries
        .orEmpty()
        .any { it.shortName?.asString() in exemptContainerAnnotations }

    private fun String.isIdiomatic(): Boolean = idiomaticSuffixes.any { endsWith(it) }

    private fun String.takeLeadingWord(): String = takeWhile { !it.isUpperCase() }
}
