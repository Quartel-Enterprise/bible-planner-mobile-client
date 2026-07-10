package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

class BiblePlannerRuleSetProvider : RuleSetProviderV3(RuleSetId(RULE_SET_ID)) {
    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        RuleProvider { PrivateTopLevelValNamingRule() },
        RuleProvider { TopLevelValPositionRule() },
        RuleProvider { WhenEntrySingleStatementBracesRule() },
        RuleProvider { FunInterfaceRule() },
        RuleProvider { RedundantSamConstructorArgumentRule() },
    )
}
