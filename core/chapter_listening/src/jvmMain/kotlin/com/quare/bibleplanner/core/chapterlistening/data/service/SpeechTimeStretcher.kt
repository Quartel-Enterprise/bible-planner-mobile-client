package com.quare.bibleplanner.core.chapterlistening.data.service

import kotlin.math.abs

/*
 * Why: Java Sound has no rate control that keeps the pitch, so speech is sped up or slowed down by
 * dropping or repeating whole pitch periods with a crossfade (pitch-synchronous overlap-add, the
 * algorithm of the Sonic library behind ExoPlayer's playback speed).
 */
internal class SpeechTimeStretcher(
    private val channels: Int,
    private val speed: Float,
    sampleRate: Int,
) {
    private val minPeriod = sampleRate / MAX_PITCH_HZ
    private val maxPeriod = sampleRate / MIN_PITCH_HZ
    private val maxRequired = 2 * maxPeriod

    // Why: the coarse pitch search runs on a 4 kHz copy, which is enough for a voice and keeps it cheap.
    private val downsampleFactor = (sampleRate / PITCH_SEARCH_RATE).coerceAtLeast(1)

    private var input = ShortArray(maxRequired * channels * 2)
    private var inputFrames = 0
    private var output = ShortArray(maxRequired * channels * 2)
    private var outputFrames = 0
    private var framesToCopy = 0
    private var previousPeriod = 0
    private var previousMinDiff = 0.0

    private val isPassThrough: Boolean
        get() = abs(speed - 1f) < SPEED_TOLERANCE

    fun transform(samples: ShortArray): ShortArray {
        if (isPassThrough) return samples
        append(samples)
        stretch()
        return takeOutput()
    }

    fun takeRemaining(): ShortArray {
        if (isPassThrough) return ShortArray(0)
        copyToOutput(
            position = 0,
            frames = inputFrames,
        )
        inputFrames = 0
        return takeOutput()
    }

    private fun append(samples: ShortArray) {
        val frames = samples.size / channels
        input = input.ensureCapacity((inputFrames + frames) * channels)
        samples.copyInto(
            destination = input,
            destinationOffset = inputFrames * channels,
        )
        inputFrames += frames
    }

    private fun stretch() {
        var position = 0
        while (inputFrames - position >= maxRequired) {
            if (framesToCopy > 0) {
                val frames = minOf(framesToCopy, maxRequired)
                copyToOutput(
                    position = position,
                    frames = frames,
                )
                framesToCopy -= frames
                position += frames
            } else {
                val period = findPitchPeriod(position)
                position += if (speed > 1f) {
                    period + skipPeriod(
                        position = position,
                        period = period,
                    )
                } else {
                    insertRepeatedPeriod(
                        position = position,
                        period = period,
                    )
                }
            }
        }
        input.copyInto(
            destination = input,
            startIndex = position * channels,
            endIndex = inputFrames * channels,
        )
        inputFrames -= position
    }

    private fun skipPeriod(
        position: Int,
        period: Int,
    ): Int {
        val frames = if (speed >= 2f) {
            (period / (speed - 1f)).toInt()
        } else {
            framesToCopy = (period * (2f - speed) / (speed - 1f)).toInt()
            period
        }
        crossfade(
            frames = frames,
            fadeOutPosition = position,
            fadeInPosition = position + period,
        )
        return frames
    }

    private fun insertRepeatedPeriod(
        position: Int,
        period: Int,
    ): Int {
        val frames = if (speed < 0.5f) {
            (period * speed / (1f - speed)).toInt()
        } else {
            framesToCopy = (period * (2f * speed - 1f) / (1f - speed)).toInt()
            period
        }
        copyToOutput(
            position = position,
            frames = period,
        )
        crossfade(
            frames = frames,
            fadeOutPosition = position + period,
            fadeInPosition = position,
        )
        return frames
    }

    private fun copyToOutput(
        position: Int,
        frames: Int,
    ) {
        output = output.ensureCapacity((outputFrames + frames) * channels)
        input.copyInto(
            destination = output,
            destinationOffset = outputFrames * channels,
            startIndex = position * channels,
            endIndex = (position + frames) * channels,
        )
        outputFrames += frames
    }

    private fun crossfade(
        frames: Int,
        fadeOutPosition: Int,
        fadeInPosition: Int,
    ) {
        output = output.ensureCapacity((outputFrames + frames) * channels)
        for (frame in 0 until frames) {
            for (channel in 0 until channels) {
                val fadeOut = input[(fadeOutPosition + frame) * channels + channel]
                val fadeIn = input[(fadeInPosition + frame) * channels + channel]
                val mixed = (fadeOut * (frames - frame) + fadeIn * frame) / frames
                output[(outputFrames + frame) * channels + channel] = mixed.toShort()
            }
        }
        outputFrames += frames
    }

    private fun takeOutput(): ShortArray {
        val drained = output.copyOf(outputFrames * channels)
        outputFrames = 0
        return drained
    }

    private fun findPitchPeriod(position: Int): Int {
        val coarse = extractMono(position)
        val coarsePeriod = searchPeriod(
            signal = coarse,
            minPeriod = minPeriod / downsampleFactor,
            maxPeriod = maxPeriod / downsampleFactor,
        ).period * downsampleFactor
        val search = searchPeriod(
            signal = extractMono(
                position = position,
                factor = 1,
            ),
            minPeriod = (coarsePeriod - REFINE_RANGE * downsampleFactor).coerceAtLeast(minPeriod),
            maxPeriod = (coarsePeriod + REFINE_RANGE * downsampleFactor).coerceAtMost(maxPeriod),
        )
        val period = if (isPreviousPeriodBetter(search)) previousPeriod else search.period
        previousMinDiff = search.minDiff
        previousPeriod = search.period
        return period
    }

    // Why: a weak new match would make the voice warble, so the last period wins unless the new one is clear.
    private fun isPreviousPeriodBetter(search: PeriodSearch): Boolean {
        if (search.minDiff == 0.0 || previousPeriod == 0) return false
        if (search.maxDiff > search.minDiff * CLEAR_MATCH_RATIO) return false
        return search.minDiff * 2 > previousMinDiff * 3
    }

    private fun extractMono(
        position: Int,
        factor: Int = downsampleFactor,
    ): IntArray {
        val length = maxRequired / factor
        return IntArray(length) { index ->
            var sum = 0
            val start = position + index * factor
            for (frame in start until start + factor) {
                for (channel in 0 until channels) sum += input[frame * channels + channel]
            }
            sum / (factor * channels)
        }
    }

    private fun searchPeriod(
        signal: IntArray,
        minPeriod: Int,
        maxPeriod: Int,
    ): PeriodSearch {
        var bestPeriod = minPeriod
        var minDiff = Double.MAX_VALUE
        var maxDiff = 0.0
        for (period in minPeriod..maxPeriod) {
            var diff = 0L
            for (index in 0 until period) diff += abs(signal[index] - signal[index + period])
            val normalized = diff.toDouble() / period
            if (normalized < minDiff) {
                minDiff = normalized
                bestPeriod = period
            }
            if (normalized > maxDiff) maxDiff = normalized
        }
        return PeriodSearch(
            period = bestPeriod,
            minDiff = minDiff,
            maxDiff = maxDiff,
        )
    }

    private fun ShortArray.ensureCapacity(size: Int): ShortArray = if (size <= this.size) this else copyOf(size * 2)

    private companion object {
        const val MIN_PITCH_HZ = 65
        const val MAX_PITCH_HZ = 400
        const val PITCH_SEARCH_RATE = 4000
        const val REFINE_RANGE = 4
        const val CLEAR_MATCH_RATIO = 3
        const val SPEED_TOLERANCE = 0.001f
    }
}
