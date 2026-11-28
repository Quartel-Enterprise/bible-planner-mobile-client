package com.quare.bibleplanner.feature.read.presentation.deletecolor

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.RemoveCustomHighlightColor
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class DeleteHighlightColorViewModel internal constructor(
    private val removeCustomHighlightColor: RemoveCustomHighlightColor,
    private val navigator: Navigator,
    route: DeleteHighlightColorNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteHighlightColorUiEvent>(trackEvent) {
    private val colorKey = route.colorKey

    val color: HighlightColor? = HighlightColor.fromKey(colorKey)

    val uiAction: SharedFlow<DeleteHighlightColorUiAction>
        field = MutableSharedFlow<DeleteHighlightColorUiAction>()

    override fun handleEvent(event: DeleteHighlightColorUiEvent) {
        when (event) {
            is DeleteHighlightColorUiEvent.OnConfirmClick -> confirmDeletion(event.shouldKeepHighlights)
            DeleteHighlightColorUiEvent.OnCancelClick -> navigator.navigateBack()
        }
    }

    private fun confirmDeletion(shouldKeepHighlights: Boolean) {
        trackEvent(
            name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_DELETED,
            params = mapOf(
                AnalyticsParams.COLOR to colorKey,
                AnalyticsParams.KEPT_HIGHLIGHTS to shouldKeepHighlights,
            ),
        )
        viewModelScope.launch {
            removeCustomHighlightColor(
                colorKey = colorKey,
                shouldKeepHighlights = shouldKeepHighlights,
            )
            uiAction.emit(DeleteHighlightColorUiAction.NotifyDeletion(hasKeptHighlights = shouldKeepHighlights))
            // Why: going back disposes the entry's action collector, so the snackbar must be emitted first.
            navigator.navigateBack()
        }
    }
}
