package com.quare.bibleplanner.core.model.loadable

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>

    data class Loaded<out T>(
        val value: T,
    ) : Loadable<T>
}

fun <T> Loadable<T>.valueOrNull(): T? = (this as? Loadable.Loaded)?.value
