package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.data.service.DesktopChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.DesktopListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.data.service.JavaSoundPcmOutput
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.utils.ioDispatcher
import com.quare.bibleplanner.core.utils.resolveAppCacheDirectory
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

private const val OUTPUT_BUFFER_SECONDS = 0.25f

internal actual val platformChapterListeningModule: Module = module {
    single { ChapterAudioCacheDirectory(Path(resolveAppCacheDirectory().path, CHAPTER_AUDIO_DIRECTORY)) }
    single<ChapterAudioPlayer> {
        DesktopChapterAudioPlayer(
            openOutput = { format -> JavaSoundPcmOutput(openSourceLine(format)) },
            playbackDispatcher = ioDispatcher,
        )
    }
    singleOf(::DesktopListeningMediaSession).bind<ListeningMediaSession>()
}

// Why: a short buffer lets a pause or a seek cut the voice right away, while still covering a slow frame.
private fun openSourceLine(format: AudioFormat): SourceDataLine {
    val line = AudioSystem.getSourceDataLine(format)
    val bufferBytes = (format.frameRate * OUTPUT_BUFFER_SECONDS).toInt() * format.frameSize
    line.open(format, bufferBytes)
    return line
}
