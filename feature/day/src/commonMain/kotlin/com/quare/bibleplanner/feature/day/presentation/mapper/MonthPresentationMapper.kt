package com.quare.bibleplanner.feature.day.presentation.mapper

import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.april
import bibleplanner.feature.day.generated.resources.august
import bibleplanner.feature.day.generated.resources.december
import bibleplanner.feature.day.generated.resources.february
import bibleplanner.feature.day.generated.resources.january
import bibleplanner.feature.day.generated.resources.july
import bibleplanner.feature.day.generated.resources.june
import bibleplanner.feature.day.generated.resources.march
import bibleplanner.feature.day.generated.resources.may
import bibleplanner.feature.day.generated.resources.november
import bibleplanner.feature.day.generated.resources.october
import bibleplanner.feature.day.generated.resources.september
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource

class MonthPresentationMapper {
    fun map(month: Month): StringResource = when (month) {
        Month.JANUARY -> Res.string.january
        Month.FEBRUARY -> Res.string.february
        Month.MARCH -> Res.string.march
        Month.APRIL -> Res.string.april
        Month.MAY -> Res.string.may
        Month.JUNE -> Res.string.june
        Month.JULY -> Res.string.july
        Month.AUGUST -> Res.string.august
        Month.SEPTEMBER -> Res.string.september
        Month.OCTOBER -> Res.string.october
        Month.NOVEMBER -> Res.string.november
        Month.DECEMBER -> Res.string.december
    }
}
