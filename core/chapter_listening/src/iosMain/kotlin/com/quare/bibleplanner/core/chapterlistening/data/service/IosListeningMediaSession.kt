package com.quare.bibleplanner.core.chapterlistening.data.service

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionModeSpokenAudio
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.AVAudioSessionRouteChangeReasonOldDeviceUnavailable
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommand
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskIdentifier
import platform.UIKit.UIBackgroundTaskInvalid
import platform.darwin.NSObjectProtocol
import kotlin.time.DurationUnit

@OptIn(ExperimentalForeignApi::class)
internal class IosListeningMediaSession : ListeningMediaSession {
    private val commandChannel = Channel<ListeningRemoteCommand>(Channel.UNLIMITED)
    override val commands: Flow<ListeningRemoteCommand> = commandChannel.receiveAsFlow()

    private val interruptionChannel = Channel<AudioInterruptionModel>(Channel.UNLIMITED)
    override val interruptions: Flow<AudioInterruptionModel> = interruptionChannel.receiveAsFlow()

    private val commandTargets = mutableListOf<Pair<MPRemoteCommand, Any>>()
    private val observers = mutableListOf<NSObjectProtocol>()
    private var isActive = false
    private var isPlaying = false

    /*
     * Why: iOS suspends a background app once nothing plays, so while the next chapter is generated and
     * downloaded between chapters, with the screen locked, the session asks for time to finish loading it.
     */
    private var loadingTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid

    // Why: an interruption deactivates the session behind the app's back, so every start activates it again.
    override fun requestActivation(): Boolean {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(
            category = AVAudioSessionCategoryPlayback,
            mode = AVAudioSessionModeSpokenAudio,
            options = 0u,
            error = null,
        )
        if (!audioSession.setActive(true, error = null)) {
            Logger.w { "Could not activate the audio session for chapter listening" }
            return false
        }
        if (!isActive) {
            registerRemoteCommands()
            observeAudioSession(audioSession)
            isActive = true
        }
        return true
    }

    override fun update(nowPlaying: NowPlayingModel) {
        isPlaying = nowPlaying.isPlaying
        if (nowPlaying.isPlaying && !nowPlaying.isAudioLoaded) beginLoadingTask() else endLoadingTask()
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf<Any?, Any?>(
            MPMediaItemPropertyTitle to
                (
                    nowPlaying.verseNumber?.let { number -> "${nowPlaying.chapterTitle}:$number" }
                        ?: nowPlaying.chapterTitle
                ),
            MPMediaItemPropertyArtist to nowPlaying.subtitle,
            MPMediaItemPropertyAlbumTitle to nowPlaying.chapterTitle,
            MPMediaItemPropertyPlaybackDuration to NSNumber(nowPlaying.time.total.toDouble(DurationUnit.SECONDS)),
            MPNowPlayingInfoPropertyElapsedPlaybackTime to
                NSNumber(nowPlaying.time.elapsed.toDouble(DurationUnit.SECONDS)),
            // Why: while the chapter loads nothing plays, so the lock screen must not move its time forward.
            MPNowPlayingInfoPropertyPlaybackRate to
                NSNumber(if (nowPlaying.isPlaying && nowPlaying.isAudioLoaded) 1.0 else 0.0),
        )
    }

    override fun deactivate() {
        endLoadingTask()
        if (!isActive) return
        isActive = false
        commandTargets.forEach { (command, target) ->
            command.removeTarget(target)
            command.enabled = false
        }
        commandTargets.clear()
        observers.forEach(NSNotificationCenter.defaultCenter::removeObserver)
        observers.clear()
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
        AVAudioSession.sharedInstance().setActive(
            active = false,
            withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
            error = null,
        )
    }

    private fun beginLoadingTask() {
        if (loadingTask != UIBackgroundTaskInvalid) return
        loadingTask = UIApplication.sharedApplication.beginBackgroundTaskWithName(
            taskName = LOADING_TASK_NAME,
            expirationHandler = ::endLoadingTask,
        )
    }

    private fun endLoadingTask() {
        if (loadingTask == UIBackgroundTaskInvalid) return
        UIApplication.sharedApplication.endBackgroundTask(loadingTask)
        loadingTask = UIBackgroundTaskInvalid
    }

    private fun registerRemoteCommands() {
        val center = MPRemoteCommandCenter.sharedCommandCenter()
        addTarget(center.playCommand) { ListeningRemoteCommand.Play }
        addTarget(center.pauseCommand) { ListeningRemoteCommand.Pause }
        addTarget(center.togglePlayPauseCommand) {
            if (isPlaying) ListeningRemoteCommand.Pause else ListeningRemoteCommand.Play
        }
        addTarget(center.nextTrackCommand) { ListeningRemoteCommand.NextVerse }
        addTarget(center.previousTrackCommand) { ListeningRemoteCommand.PreviousVerse }
    }

    private fun addTarget(
        command: MPRemoteCommand,
        createCommand: () -> ListeningRemoteCommand,
    ) {
        command.enabled = true
        val target = command.addTargetWithHandler { _ ->
            commandChannel.trySend(createCommand())
            MPRemoteCommandHandlerStatusSuccess
        }
        commandTargets += command to target
    }

    private fun observeAudioSession(audioSession: AVAudioSession) {
        observers += NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVAudioSessionInterruptionNotification,
            `object` = audioSession,
            queue = NSOperationQueue.mainQueue,
            usingBlock = ::onInterruption,
        )
        observers += NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVAudioSessionRouteChangeNotification,
            `object` = audioSession,
            queue = NSOperationQueue.mainQueue,
            usingBlock = ::onRouteChange,
        )
    }

    private fun onInterruption(notification: NSNotification?) {
        val userInfo = notification?.userInfo
        val type = (userInfo?.get(AVAudioSessionInterruptionTypeKey) as? NSNumber)?.unsignedLongValue
        val options = (userInfo?.get(AVAudioSessionInterruptionOptionKey) as? NSNumber)?.unsignedLongValue ?: 0u
        val shouldResume = options and AVAudioSessionInterruptionOptionShouldResume != 0uL
        when {
            type == AVAudioSessionInterruptionTypeBegan -> interruptionChannel.trySend(AudioInterruptionModel.BEGAN)

            type == AVAudioSessionInterruptionTypeEnded && shouldResume ->
                interruptionChannel.trySend(AudioInterruptionModel.ENDED)
        }
    }

    // Why: unplugging headphones or losing Bluetooth must pause instead of reading out loud.
    private fun onRouteChange(notification: NSNotification?) {
        val reason = (notification?.userInfo?.get(AVAudioSessionRouteChangeReasonKey) as? NSNumber)?.unsignedLongValue
        if (reason == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
            interruptionChannel.trySend(AudioInterruptionModel.PAUSE_REQUESTED)
        }
    }

    private companion object {
        const val LOADING_TASK_NAME = "ChapterListeningLoading"
    }
}
