package com.quare.bibleplanner.feature.subscriptiondetails.presentation.mapper

import bibleplanner.feature.subscription_details.generated.resources.Res
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro_annual
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro_monthly
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProPlanTypeStringMapperTest {
    @Test
    fun `GIVEN a monthly plan WHEN mapping it THEN names the monthly pro plan`() {
        // Given
        val plan = ProPlanType.MONTHLY

        // When
        val label = plan.toStringResource()

        // Then
        assertEquals(Res.string.plan_value_pro_monthly, label)
    }

    @Test
    fun `GIVEN an annual plan WHEN mapping it THEN names the annual pro plan`() {
        // Given
        val plan = ProPlanType.ANNUAL

        // When
        val label = plan.toStringResource()

        // Then
        assertEquals(Res.string.plan_value_pro_annual, label)
    }

    @Test
    fun `GIVEN an unknown plan WHEN mapping it THEN names the generic pro plan`() {
        // Given
        val plan = ProPlanType.UNKNOWN

        // When
        val label = plan.toStringResource()

        // Then
        assertEquals(Res.string.plan_value_pro, label)
    }
}
