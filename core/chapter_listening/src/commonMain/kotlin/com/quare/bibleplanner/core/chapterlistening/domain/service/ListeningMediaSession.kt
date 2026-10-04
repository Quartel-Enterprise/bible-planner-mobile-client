package com.quare.bibleplanner.core.chapterlistening.domain.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import kotlinx.coroutines.flow.Flow

interface ListeningMediaSession {
    val commands: Flow<ListeningRemoteCommand>
    val interruptions: Flow<AudioInterruptionModel>

    fun requestActivation(): Boolean

    fun update(nowPlaying: NowPlayingModel)

    fun deactivate()
}
