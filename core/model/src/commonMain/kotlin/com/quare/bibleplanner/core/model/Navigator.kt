package com.quare.bibleplanner.core.model

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class Navigator {
    private val _commands: Channel<NavigationCommand> = Channel(Channel.BUFFERED)
    val commands: Flow<NavigationCommand> = _commands.receiveAsFlow()

    fun navigate(route: NavKey) {
        send(NavigationCommand.Navigate(route))
    }

    fun navigateReplacingTop(route: NavKey) {
        send(NavigationCommand.NavigateReplacingTop(route))
    }

    fun navigateBack() {
        send(NavigationCommand.NavigateBack)
    }

    private fun send(command: NavigationCommand) {
        _commands.trySend(command)
    }
}
