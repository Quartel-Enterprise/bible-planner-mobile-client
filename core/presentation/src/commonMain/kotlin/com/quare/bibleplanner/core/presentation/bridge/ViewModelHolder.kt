package com.quare.bibleplanner.core.presentation.bridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.reflect.KClass

/*
 * Why: outside Compose nothing owns a ViewModelStore, so SwiftUI and React keep one per screen and
 * clear it when the screen goes away, which cancels viewModelScope like Android does. The composition
 * root builds each holder on the Kotlin side, so Swift and JS never pass the KClass themselves.
 */
class ViewModelHolder<VM : ViewModel>(
    kClass: KClass<VM>,
    createViewModel: () -> VM,
) {
    private val store = ViewModelStore()

    // Why: dropped on clear() so a holder Swift or JS still references doesn't keep the cleared ViewModel alive.
    private var createdViewModel: VM? = ViewModelProvider
        .create(
            store = store,
            factory = viewModelFactory { addInitializer(kClass) { createViewModel() } },
        )[kClass]

    // Why: clear() ends the holder's life; reading a cancelled ViewModel would leave a screen silently dead.
    val viewModel: VM
        get() = checkNotNull(createdViewModel) { "ViewModelHolder was cleared; create a new one for the screen." }

    fun clear() {
        createdViewModel = null
        store.clear()
    }
}
