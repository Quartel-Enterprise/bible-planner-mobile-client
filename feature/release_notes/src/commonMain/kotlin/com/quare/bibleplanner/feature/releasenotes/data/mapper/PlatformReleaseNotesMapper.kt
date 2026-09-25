package com.quare.bibleplanner.feature.releasenotes.data.mapper

import com.quare.bibleplanner.core.provider.platform.Platform

class PlatformReleaseNotesMapper(
    platform: Platform,
) {
    private val platformBucket: String = when (platform) {
        Platform.Android -> ANDROID_BUCKET
        Platform.Ios -> IOS_BUCKET
        is Platform.Desktop -> DESKTOP_BUCKET
    }

    fun mapToPlatformChanges(releaseNotes: Map<String, Map<String, List<String>>>): Map<String, List<String>> =
        releaseNotes
            .mapValues { (_, buckets) ->
                buckets[COMMON_BUCKET].orEmpty() + buckets[platformBucket].orEmpty()
            }.filterValues(List<String>::isNotEmpty)

    companion object {
        private const val COMMON_BUCKET = "common"
        private const val ANDROID_BUCKET = "android"
        private const val IOS_BUCKET = "ios"
        private const val DESKTOP_BUCKET = "desktop"
    }
}
