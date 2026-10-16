package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// Why: a desktop app shares the speakers with every other app, so there is no audio focus to ask for or lose.
internal class DesktopListeningMediaSession : ListeningMediaSession {
    override val commands: Flow<ListeningRemoteCommand> = emptyFlow()
    override val interruptions: Flow<AudioInterruptionModel> = emptyFlow()

    override fun requestActivation(): Boolean = true

    override fun update(nowPlaying: NowPlayingModel) {
    }

    override fun deactivate() {
    }
}
