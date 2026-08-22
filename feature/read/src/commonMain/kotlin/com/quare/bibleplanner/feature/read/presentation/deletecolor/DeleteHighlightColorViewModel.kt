package com.quare.bibleplanner.feature.read.presentation.deletecolor

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.highlight_color_removed
import bibleplanner.feature.read.generated.resources.highlight_color_removed_kept
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

internal class DeleteHighlightColorViewModel(
    route: DeleteHighlightColorNavRoute,
    private val removeCustomHighlightColor: RemoveCustomHighlightColor,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteHighlightColorUiEvent>(trackEvent) {
    private val colorKey = route.colorKey

    val color: HighlightColor? = HighlightColor.fromKey(colorKey)

    private val _uiAction = MutableSharedFlow<DeleteHighlightColorUiAction>()
    val uiAction: SharedFlow<DeleteHighlightColorUiAction> = _uiAction

    override fun handleEvent(event: DeleteHighlightColorUiEvent) {
        when (event) {
            is DeleteHighlightColorUiEvent.OnConfirmClick -> confirmDeletion(event.shouldKeepHighlights)

            DeleteHighlightColorUiEvent.OnCancelClick ->
                viewModelScope.launch { _uiAction.emit(DeleteHighlightColorUiAction.NavigateBack) }
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
            _uiAction.emit(
                DeleteHighlightColorUiAction.NavigateBackWithMessage(
                    if (shouldKeepHighlights) {
                        Res.string.highlight_color_removed_kept
                    } else {
                        Res.string.highlight_color_removed
                    },
                ),
            )
        }
    }
}
