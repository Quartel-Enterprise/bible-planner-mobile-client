package com.quare.bibleplanner.tools.agentcli.di

import com.quare.bibleplanner.tools.agentcli.log.LogEntry
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

internal class RecordingAnalyticsServiceTest {
    private val log = SessionLog(TimeSource.Monotonic)
    private val service = RecordingAnalyticsService(log)

    @Test
    fun `GIVEN the recording service WHEN tracking events and user properties THEN records them in the session log`() =
        runTest {
            // Given
            val eventParams = mapOf("day_number" to 1, "is_read" to true, "source" to "day_screen")

            // When
            service.logEvent(
                name = "day_read_toggled",
                params = eventParams,
            )
            service.setUserProperty(
                name = "is_tester",
                value = "false",
            )
            val appInstanceId = service.getAppInstanceId()

            // Then
            assertEquals(
                expected = listOf(
                    LogEntry(
                        kind = LogKind.ANALYTICS,
                        source = "day_read_toggled",
                        payload = Json.parseToJsonElement(
                            """{"day_number": 1, "is_read": true, "source": "day_screen"}""",
                        ),
                    ),
                    LogEntry(
                        kind = LogKind.ANALYTICS,
                        source = "user_property",
                        payload = Json.parseToJsonElement("""{"is_tester": "false"}"""),
                    ),
                ),
                actual = log.takeEntries(),
            )
            assertEquals(
                expected = "agent-cli",
                actual = appInstanceId,
            )
        }
}
