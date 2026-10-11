package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.Flow

/*
 * Why: connecting a MediaController is what starts the MediaSessionService; Media3 then moves it to
 * the foreground with the media notification while the chapter plays, which keeps the process alive
 * with the screen off. Audio focus and unplugged headphones are handled by the ExoPlayer itself.
 */
internal class AndroidListeningMediaSession(
    private val context: Context,
    private val player: ListeningPlayer,
) : ListeningMediaSession {
    override val commands: Flow<ListeningRemoteCommand> = player.commands
    override val interruptions: Flow<AudioInterruptionModel> = player.interruptions

    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun requestActivation(): Boolean {
        if (controllerFuture == null) {
            controllerFuture = MediaController
                .Builder(
                    context,
                    SessionToken(context, ComponentName(context, ChapterListeningService::class.java)),
                ).buildAsync()
        }
        return true
    }

    override fun update(nowPlaying: NowPlayingModel) {
        player.show(nowPlaying)
    }

    override fun deactivate() {
        player.show(null)
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
    }
}
