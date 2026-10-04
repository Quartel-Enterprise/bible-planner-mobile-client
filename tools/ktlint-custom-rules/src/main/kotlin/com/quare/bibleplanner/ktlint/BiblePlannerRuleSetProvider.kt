package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

class BiblePlannerRuleSetProvider : RuleSetProviderV3(RuleSetId(RULE_SET_ID)) {
    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        RuleProvider { PrivateTopLevelValNamingRule() },
        RuleProvider { TopLevelValPositionRule() },
        RuleProvider { TopLevelValOwnershipRule() },
        RuleProvider { TopLevelValBlankLineRule() },
        RuleProvider { WhenEntrySingleStatementBracesRule() },
        RuleProvider { FunInterfaceRule() },
        RuleProvider { InterfaceImplementationSeparateFilesRule() },
        RuleProvider { RedundantSamConstructorArgumentRule() },
        RuleProvider { ValueReturningFunctionNamingRule() },
        RuleProvider { UnitFunctionBlockBodyRule() },
        RuleProvider { PreferMethodReferenceRule() },
        RuleProvider { ExplicitBackingFieldRule() },
        RuleProvider { CompanionObjectConstantsRule() },
        RuleProvider { RedundantPrivateConstructorPropertyRule() },
        RuleProvider { DtoSerialNameRule() },
        RuleProvider { UnusedFunctionParameterRule() },
        RuleProvider { ComposableNamingSuffixRule() },
        RuleProvider { ConstructorPropertyOrderRule() },
        RuleProvider { DataClassDefaultValueRule() },
        RuleProvider { DataClassOwnFileRule() },
        RuleProvider { TwoBranchWhenRule() },
        RuleProvider { CompanionObjectDurationRule() },
        RuleProvider { SuspendRunCatchingRule() },
        RuleProvider { CommentsSayWhyRule() },
        RuleProvider { TestNameGivenWhenThenRule() },
        RuleProvider { TestBodySectionsRule() },
        RuleProvider { PrepareScenarioReturnsUnitRule() },
        RuleProvider { PrepareScenarioLastMemberRule() },
        RuleProvider { PrepareScenarioInGivenRule() },
    )
}
