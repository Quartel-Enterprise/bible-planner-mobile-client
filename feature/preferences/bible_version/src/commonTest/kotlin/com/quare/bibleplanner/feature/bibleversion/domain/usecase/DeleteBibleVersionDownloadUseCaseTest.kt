package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.feature.bibleversion.fake.ThrowingBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.ThrowingVerseDao
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DeleteBibleVersionDownloadUseCaseTest {
    private val versionId = "acf"
    private lateinit var useCase: DeleteBibleVersionDownloadUseCase
    private lateinit var calls: MutableList<String>

    @BeforeTest
    fun setUp() {
        calls = mutableListOf()
        useCase = DeleteBibleVersionDownloadUseCase(
            bibleVersionDao = RecordingBibleVersionDao(calls),
            verseDao = RecordingVerseDao(calls),
            notifier = RecordingNotifier(calls),
        )
    }

    @Test
    fun `GIVEN an ongoing download WHEN deleting THEN dismisses its notification`() = runTest {
        // When
        useCase(versionId)

        // Then
        assertEquals("dismiss $versionId", calls.first())
    }

    @Test
    fun `GIVEN an ongoing download WHEN deleting THEN marks it as not started before wiping its verses`() = runTest {
        // When
        useCase(versionId)

        // Then
        assertEquals(
            listOf(
                "status $versionId ${DownloadStatus.NOT_STARTED}",
                "deleteVerseTexts $versionId",
            ),
            calls.drop(1),
        )
    }
}

private class RecordingBibleVersionDao(
    private val calls: MutableList<String>,
) : ThrowingBibleVersionDao() {
    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) {
        calls += "status $id $status"
    }
}

private class RecordingVerseDao(
    private val calls: MutableList<String>,
) : ThrowingVerseDao() {
    override suspend fun deleteVerseTextsByVersion(versionId: String) {
        calls += "deleteVerseTexts $versionId"
    }
}

private class RecordingNotifier(
    private val calls: MutableList<String>,
) : BibleVersionDownloadNotifier {
    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        error("Unexpected call")
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        error("Unexpected call")
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        error("Unexpected call")
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        error("Unexpected call")
    }

    override suspend fun dismiss(versionId: String) {
        calls += "dismiss $versionId"
    }
}
