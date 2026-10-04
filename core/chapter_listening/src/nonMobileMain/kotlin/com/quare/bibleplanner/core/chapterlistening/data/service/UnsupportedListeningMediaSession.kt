package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class UnsupportedListeningMediaSession : ListeningMediaSession {
    override val commands: Flow<ListeningRemoteCommand> = emptyFlow()
    override val interruptions: Flow<AudioInterruptionModel> = emptyFlow()

    override fun requestActivation(): Boolean = false

    override fun update(nowPlaying: NowPlayingModel) {
    }

    override fun deactivate() {
    }
}
