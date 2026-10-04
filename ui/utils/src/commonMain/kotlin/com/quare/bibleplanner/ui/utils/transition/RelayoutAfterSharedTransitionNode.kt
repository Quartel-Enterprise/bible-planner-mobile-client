package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidatePlacement
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch

internal class RelayoutAfterSharedTransitionNode(
    private var sharedTransitionScope: SharedTransitionScope,
) : Modifier.Node(),
    LayoutModifierNode,
    ObserverModifierNode {
    private var wasTransitionActive = false

    fun updateSharedTransitionScope(newSharedTransitionScope: SharedTransitionScope) {
        if (newSharedTransitionScope == sharedTransitionScope) return
        sharedTransitionScope = newSharedTransitionScope
        wasTransitionActive = false
        if (isAttached) {
            observeTransitionActivity()
        }
    }

    override fun onAttach() {
        observeTransitionActivity()
    }

    override fun onDetach() {
        wasTransitionActive = false
    }

    override fun onObservedReadsChanged() {
        observeTransitionActivity()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(
            width = placeable.width,
            height = placeable.height,
        ) {
            placeable.place(
                x = 0,
                y = 0,
            )
        }
    }

    private fun observeTransitionActivity() {
        observeReads {
            val isTransitionActive = sharedTransitionScope.isTransitionActive
            if (wasTransitionActive && !isTransitionActive) {
                invalidatePlacementOnNextFrame()
            }
            wasTransitionActive = isTransitionActive
        }
    }

    private fun invalidatePlacementOnNextFrame() {
        coroutineScope.launch {
            withFrameNanos { }
            invalidatePlacement()
        }
    }
}
