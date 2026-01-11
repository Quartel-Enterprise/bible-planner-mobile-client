package com.quare.bibleplanner.feature.releasenotes.domain.utils

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel

object VersionComparator : Comparator<ReleaseNoteModel> {
    override fun compare(
        a: ReleaseNoteModel,
        b: ReleaseNoteModel,
    ): Int = compare(a.version, b.version)

    fun compare(
        v1: String,
        v2: String,
    ): Int {
        val v1Parts = parseVersion(v1)
        val v2Parts = parseVersion(v2)
        val length = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until length) {
            val comparison = v1Parts[i].compareTo(v2Parts[i])
            if (comparison != 0) return comparison
        }
        return 0
    }

    private fun parseVersion(version: String): List<Int> {
        val parts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = parts.size.coerceAtLeast(3)
        return List(maxLength) { i -> parts.getOrElse(i) { 0 } }
    }
}
