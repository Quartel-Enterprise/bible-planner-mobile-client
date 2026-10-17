package com.quare.bibleplanner.core.chapterlistening.data.exception

internal class ChapterAudioOutputStalledException :
    IllegalStateException("The audio output stopped playing the chapter audio")
