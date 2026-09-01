package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel

internal class AddNotesFreeWarningViewModel(
    route: AddNotesFreeWarningNavRoute,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<AddNotesFreeWarningUiEvent>(trackEvent) {
    val maxFreeNotesAmount = route.maxFreeNotesAmount

    override fun handleEvent(event: AddNotesFreeWarningUiEvent) = when (event) {
        AddNotesFreeWarningUiEvent.OnCancel -> navigator.navigateBack()

        AddNotesFreeWarningUiEvent.OnSubscribeToPro -> navigator.navigateReplacingTop(
            PaywallNavRoute(PaywallEntrySource.NOTES_LIMIT),
        )
    }
}
