package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.MutableSharedFlow

internal class FakeListeningMediaSession(
    private val canActivate: Boolean,
) : ListeningMediaSession {
    override val commands = MutableSharedFlow<ListeningRemoteCommand>(extraBufferCapacity = 16)
    override val interruptions = MutableSharedFlow<AudioInterruptionModel>(extraBufferCapacity = 16)
    val updates = mutableListOf<NowPlayingModel>()
    var deactivationCount = 0

    override fun requestActivation(): Boolean = canActivate

    override fun update(nowPlaying: NowPlayingModel) {
        updates += nowPlaying
    }

    override fun deactivate() {
        deactivationCount++
    }
}
