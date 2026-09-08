package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId

private val biblePlannerAbout = About(
    maintainer = "Bible Planner",
    repositoryUrl = "https://github.com/quare-tech/bible-planner-mobile-client",
    issueTrackerUrl = "https://github.com/quare-tech/bible-planner-mobile-client/issues",
)

/**
 * Base class for every custom rule in this rule set: fills in [RuleId] (prefixed with [RULE_SET_ID]) and the
 * shared [About] block so each rule only has to name itself.
 */
abstract class BiblePlannerRule(
    id: String,
) : Rule(ruleId = RuleId("$RULE_SET_ID:$id"), about = biblePlannerAbout),
    RuleAutocorrectApproveHandler
