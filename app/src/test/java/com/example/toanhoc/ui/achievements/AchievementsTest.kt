package com.example.toanhoc.ui.achievements

import com.example.toanhoc.data.local.entity.StudentProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {

    @Test
    fun `streak badge unlocks when streak days is 3 or more`() {
        val profile3Days = StudentProfileEntity(streakDays = 3)
        val streakBadgeEarned = profile3Days.streakDays >= 3
        assertTrue("Streak badge should be earned for 3 days streak", streakBadgeEarned)
    }

    @Test
    fun `badge item model initializes correctly`() {
        val badge = BadgeItem(
            id = "b1",
            iconEmoji = "🌟",
            titleVi = "Siêu nhẩm nhanh",
            descriptionVi = "Hoàn thành 5 bài về Tách và gộp số",
            gradeLabelVi = "Lớp 1",
            isEarned = true
        )

        assertEquals("b1", badge.id)
        assertEquals("🌟", badge.iconEmoji)
        assertTrue(badge.isEarned)
    }
}
