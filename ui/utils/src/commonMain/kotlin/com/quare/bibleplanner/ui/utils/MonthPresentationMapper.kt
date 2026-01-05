package com.quare.bibleplanner.ui.utils

import bibleplanner.ui.utils.generated.resources.Res
import bibleplanner.ui.utils.generated.resources.april
import bibleplanner.ui.utils.generated.resources.august
import bibleplanner.ui.utils.generated.resources.december
import bibleplanner.ui.utils.generated.resources.february
import bibleplanner.ui.utils.generated.resources.january
import bibleplanner.ui.utils.generated.resources.july
import bibleplanner.ui.utils.generated.resources.june
import bibleplanner.ui.utils.generated.resources.march
import bibleplanner.ui.utils.generated.resources.may
import bibleplanner.ui.utils.generated.resources.november
import bibleplanner.ui.utils.generated.resources.october
import bibleplanner.ui.utils.generated.resources.september
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource

class MonthPresentationMapper {
    fun map(month: Month): StringResource = month.toStringResource()
}

fun Month.toStringResource(): StringResource = when (this) {
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
