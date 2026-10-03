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

abstract class BiblePlannerRule(
    id: String,
) : Rule(ruleId = RuleId("$RULE_SET_ID:$id"), about = biblePlannerAbout),
    RuleAutocorrectApproveHandler
