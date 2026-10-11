package com.quare.bibleplanner.core.chapterlistening.data.service

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAudioTimePitchAlgorithmSpectral
import platform.AVFoundation.AVErrorFailedToParse
import platform.AVFoundation.AVErrorFileFormatNotRecognized
import platform.AVFoundation.AVFoundationErrorDomain
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeErrorKey
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerItemStatusUnknown
import platform.AVFoundation.audioTimePitchAlgorithm
import platform.AVFoundation.currentTime
import platform.AVFoundation.pause
import platform.AVFoundation.playImmediatelyAtRate
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setRate
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.kCMTimeZero
import platform.Foundation.NSError
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalForeignApi::class)
internal class IosChapterAudioPlayer : ChapterAudioPlayer {
    // Why: only these mean the cached file itself is broken; a decoder or output failure must not delete it.
    private val unreadableFileErrorCodes = setOf(
        AVErrorFileFormatNotRecognized,
        AVErrorFailedToParse,
    )

    override val isSupported: Boolean = true

    private val eventChannel = Channel<ChapterAudioPlayerEvent>(Channel.UNLIMITED)
    override val events: Flow<ChapterAudioPlayerEvent> = eventChannel.receiveAsFlow()

    // Why: created on the first play, so checking support or opening the reader never builds an AVPlayer.
    private val player: AVPlayer by lazy {
        // Why: an end queued before a new play of the same item reseeks it arrives while that seek is pending.
        observe(AVPlayerItemDidPlayToEndTimeNotification) {
            if (seekTarget == null) send(ChapterAudioPlayerEvent.Ended(playbackId))
        }
        // Why: a file that fails partway through carries its error in the notification, not on the item.
        observe(AVPlayerItemFailedToPlayToEndTimeNotification) { notification ->
            reportFailure(notification.userInfo?.get(AVPlayerItemFailedToPlayToEndTimeErrorKey) as? NSError)
        }
        AVPlayer()
    }
    private val statusScope = MainScope()
    private val statusPollInterval: Duration = 100.milliseconds
    private var statusJob: Job? = null
    private var loadedItem: AVPlayerItem? = null
    private var loadedPath: String? = null
    private var playbackId = 0
    private var hasReportedFailure = false
    private var seekTarget: Duration? = null
    private var seekCount = 0

    // Why: AVFoundation can't seek a new item with a completion handler before it is ready to play.
    private var pendingStartSpeed: Float? = null

    override val position: Duration
        get() {
            if (loadedItem == null) return Duration.ZERO
            seekTarget?.let { target -> return target }
            val seconds = CMTimeGetSeconds(player.currentTime())
            return if (seconds.isNaN()) Duration.ZERO else seconds.seconds
        }

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
        this.playbackId = playbackId
        if (loadedPath != audioPath) {
            val item = AVPlayerItem(uRL = NSURL.fileURLWithPath(audioPath))
            // Why: a faster or slower rate keeps the voice's pitch only with a time-pitch algorithm.
            item.audioTimePitchAlgorithm = AVAudioTimePitchAlgorithmSpectral
            player.replaceCurrentItemWithPlayerItem(item)
            loadedItem = item
            loadedPath = audioPath
            hasReportedFailure = false
            watchStatus(item)
        }
        if (loadedItem?.status != AVPlayerItemStatusReadyToPlay) {
            // Why: a seek still pending on the replaced item must not clear this start when it completes.
            seekCount++
            seekTarget = position
            pendingStartSpeed = speed
            return
        }
        seek(position)
        player.playImmediatelyAtRate(speed)
    }

    override fun seekTo(position: Duration) {
        if (loadedItem?.status == AVPlayerItemStatusReadyToPlay) seek(position)
    }

    /*
     * Why: an item that fails to open posts no notification, only its status changes, and Kotlin
     * can't override the KVO callback, so the player follows the status itself until it settles.
     */
    private fun watchStatus(item: AVPlayerItem) {
        statusJob?.cancel()
        statusJob = statusScope.launch {
            while (item.status == AVPlayerItemStatusUnknown) delay(statusPollInterval)
            if (item != loadedItem) return@launch
            if (item.status == AVPlayerItemStatusFailed) {
                reportFailure(item.error)
            } else {
                startPending()
            }
        }
    }

    private fun startPending() {
        val speed = pendingStartSpeed ?: return
        pendingStartSpeed = null
        seek(seekTarget ?: Duration.ZERO)
        player.playImmediatelyAtRate(speed)
    }

    // Why: the seek completes later, and until then currentTime still reports the old position.
    private fun seek(position: Duration) {
        val seekId = ++seekCount
        seekTarget = position
        player.seekToTime(
            time = CMTimeMakeWithSeconds(position.toDouble(DurationUnit.SECONDS), TIMESCALE),
            toleranceBefore = kCMTimeZero.readValue(),
            toleranceAfter = kCMTimeZero.readValue(),
            completionHandler = {
                dispatch_async(dispatch_get_main_queue()) {
                    if (seekId == seekCount) seekTarget = null
                }
            },
        )
    }

    override fun setSpeed(speed: Float) {
        if (pendingStartSpeed != null) pendingStartSpeed = speed
        if (loadedItem != null && player.rate != 0f) player.setRate(speed)
    }

    override fun pause() {
        pendingStartSpeed = null
        if (loadedItem != null) player.pause()
    }

    override fun release() {
        pendingStartSpeed = null
        if (loadedItem == null) return
        statusJob?.cancel()
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
        seekTarget = null
        loadedItem = null
        loadedPath = null
    }

    private fun reportFailure(error: NSError?) {
        if (hasReportedFailure) return
        hasReportedFailure = true
        Logger.e { "Could not play the chapter audio: ${error?.localizedDescription}" }
        loadedPath = null
        val isAvFoundationError = error != null && error.domain == AVFoundationErrorDomain
        send(
            ChapterAudioPlayerEvent.Failed(
                playbackId = playbackId,
                isFileUnreadable = isAvFoundationError && error?.code in unreadableFileErrorCodes,
            ),
        )
    }

    private fun observe(
        name: String?,
        onEvent: (NSNotification) -> Unit,
    ) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = name,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification: NSNotification? ->
            if (notification != null && notification.`object` == loadedItem) onEvent(notification)
        }
    }

    private fun send(event: ChapterAudioPlayerEvent) {
        eventChannel.trySend(event)
    }

    private companion object {
        const val TIMESCALE = 1000
    }
}
