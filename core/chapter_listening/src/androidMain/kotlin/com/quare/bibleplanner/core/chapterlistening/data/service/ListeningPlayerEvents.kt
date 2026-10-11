package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// Why: the controller listens from the start, while the session player and its ExoPlayer exist only once a chapter plays.
internal class ListeningPlayerEvents {
    private val commandChannel = Channel<ListeningRemoteCommand>(Channel.UNLIMITED)
    val commands: Flow<ListeningRemoteCommand> = commandChannel.receiveAsFlow()

    private val interruptionChannel = Channel<AudioInterruptionModel>(Channel.UNLIMITED)
    val interruptions: Flow<AudioInterruptionModel> = interruptionChannel.receiveAsFlow()

    fun send(command: ListeningRemoteCommand) {
        commandChannel.trySend(command)
    }

    fun interrupt(interruption: AudioInterruptionModel) {
        interruptionChannel.trySend(interruption)
    }
}
