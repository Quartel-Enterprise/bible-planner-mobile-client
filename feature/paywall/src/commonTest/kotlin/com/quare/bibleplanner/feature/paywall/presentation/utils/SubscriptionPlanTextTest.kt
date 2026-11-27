package com.quare.bibleplanner.feature.paywall.presentation.utils

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.month
import bibleplanner.feature.paywall.generated.resources.per_month
import bibleplanner.feature.paywall.generated.resources.per_year
import bibleplanner.feature.paywall.generated.resources.plan_annual
import bibleplanner.feature.paywall.generated.resources.plan_annual_description
import bibleplanner.feature.paywall.generated.resources.plan_monthly
import bibleplanner.feature.paywall.generated.resources.plan_monthly_description
import bibleplanner.feature.paywall.generated.resources.year
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SubscriptionPlanTextTest {
    @Test
    fun `GIVEN each plan WHEN mapping its texts THEN picks the title description and periods of that plan`() {
        // Given
        val plans = SubscriptionPlanType.entries

        // When
        val texts = plans.associateWith { plan ->
            listOf(
                plan.toTitleResource(),
                plan.toDescriptionResource(),
                plan.toPeriodResource(),
                plan.toPeriodUnitResource(),
            )
        }

        // Then
        assertEquals(
            mapOf<SubscriptionPlanType, List<StringResource>>(
                SubscriptionPlanType.Monthly to listOf(
                    Res.string.plan_monthly,
                    Res.string.plan_monthly_description,
                    Res.string.per_month,
                    Res.string.month,
                ),
                SubscriptionPlanType.Annual to listOf(
                    Res.string.plan_annual,
                    Res.string.plan_annual_description,
                    Res.string.per_year,
                    Res.string.year,
                ),
            ),
            texts,
        )
    }
}
