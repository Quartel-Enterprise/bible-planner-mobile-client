package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

class TestBodySectionsRule : BiblePlannerRule("test-body-sections") {
    private val setUpAnnotations = setOf("BeforeTest", "Before", "BeforeEach")
    private val sectionsWithoutGiven = listOf(TestSection.WHEN, TestSection.THEN)

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        if (!function.isTest()) return
        val sections = function.findTestSections().map { (section, _) -> section }
        val hasSetUp = function.hasSetUpBeforeEachTest()
        if (sections == TestSection.entries || (hasSetUp && sections == sectionsWithoutGiven)) return
        val identifier = function.nameIdentifier ?: return

        val givenException = if (hasSetUp) {
            " ('${TestSection.GIVEN.comment}' may be left out, since the class sets up before each test)"
        } else {
            ""
        }
        emit(
            identifier.textOffset,
            "Test '${function.name}' splits its body with '${TestSection.GIVEN.comment}', " +
                "'${TestSection.WHEN.comment}' and '${TestSection.THEN.comment}' comments, once each and in that " +
                "order$givenException",
            false,
        )
    }

    private fun KtNamedFunction.hasSetUpBeforeEachTest(): Boolean =
        containingClassOrObject?.hasSetUpBeforeEachTest() == true

    // Why: ktlint sees one file at a time, so only a superclass declared in the same file can be inspected.
    private fun KtClassOrObject.hasSetUpBeforeEachTest(): Boolean {
        val hasOwnSetUp = declarations.any { declaration ->
            declaration is KtNamedFunction && declaration.hasAnnotation(setUpAnnotations)
        }
        return hasOwnSetUp || findSuperclassesInFile().any { superclass -> superclass.hasSetUpBeforeEachTest() }
    }

    private fun KtClassOrObject.findSuperclassesInFile(): List<KtClassOrObject> {
        val superTypeNames = superTypeListEntries.mapNotNull { entry -> entry.typeAsUserType?.referencedName }
        return containingKtFile.declarations
            .filterIsInstance<KtClassOrObject>()
            .filter { declaration -> declaration != this && declaration.name in superTypeNames }
    }
}
