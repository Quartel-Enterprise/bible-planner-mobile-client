package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.node.ModifierNodeElement

internal data class RelayoutAfterSharedTransitionElement(
    private val sharedTransitionScope: SharedTransitionScope,
) : ModifierNodeElement<RelayoutAfterSharedTransitionNode>() {
    override fun create(): RelayoutAfterSharedTransitionNode = RelayoutAfterSharedTransitionNode(sharedTransitionScope)

    override fun update(node: RelayoutAfterSharedTransitionNode) {
        node.updateSharedTransitionScope(sharedTransitionScope)
    }
}
