package com.quare.bibleplanner.feature.releasenotes.domain.utils

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel

object VersionComparator : Comparator<ReleaseNoteModel> {
    override fun compare(
        o1: ReleaseNoteModel,
        o2: ReleaseNoteModel,
    ): Int {
        val v1Parts = o1.version.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = o2.version.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until length) {
            val part1 = v1Parts.getOrElse(i) { 0 }
            val part2 = v2Parts.getOrElse(i) { 0 }
            if (part1 != part2) {
                return part1.compareTo(part2)
            }
        }
        return 0
    }

    fun compare(
        v1: String,
        v2: String,
    ): Int {
        val v1Parts = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until length) {
            val part1 = v1Parts.getOrElse(i) { 0 }
            val part2 = v2Parts.getOrElse(i) { 0 }
            if (part1 != part2) {
                return part1.compareTo(part2)
            }
        }
        return 0
    }
}
