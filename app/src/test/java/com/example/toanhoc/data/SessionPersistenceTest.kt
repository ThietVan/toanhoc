package com.example.toanhoc.data

import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPersistenceTest {

    @Test
    fun `incomplete session log stores partial completed seconds correctly`() {
        val partialLog = DailySessionLogEntity(
            dateIso = "2026-03-29",
            completedSeconds = 350,
            phase1Accuracy = 0.85f,
            phase1AvgLatencyMs = 1800L,
            phase2TargetNodeId = "G1_NUM_01",
            isCompleted = false
        )

        assertEquals("2026-03-29", partialLog.dateIso)
        assertEquals(350, partialLog.completedSeconds)
        assertFalse("Session with <900s should not be marked as completed", partialLog.isCompleted)
    }

    @Test
    fun `completed session log stores full 900 seconds and isCompleted flag`() {
        val completedLog = DailySessionLogEntity(
            dateIso = "2026-03-29",
            completedSeconds = 900,
            phase1Accuracy = 0.92f,
            phase1AvgLatencyMs = 1400L,
            phase2TargetNodeId = "G1_NUM_01",
            isCompleted = true
        )

        assertEquals(900, completedLog.completedSeconds)
        assertTrue("Session with 900s should be marked as completed", completedLog.isCompleted)
    }
}
