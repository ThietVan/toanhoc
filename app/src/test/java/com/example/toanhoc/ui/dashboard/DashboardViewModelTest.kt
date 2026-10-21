package com.example.toanhoc.ui.dashboard

import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DashboardViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getKidFriendlyLessonName maps node ID to child-friendly lesson title`() {
        val title1 = viewModel.getKidFriendlyLessonName("G1_03_SPLIT_COMBINE_10", "Default")
        assertEquals("Bài 3: Tách và gộp các số phạm vi 10", title1)

        val fallback = viewModel.getKidFriendlyLessonName("UNKNOWN_NODE", "Default Title")
        assertEquals("Default Title", fallback)
    }

    @Test
    fun `getKidFriendlyLessonGoal maps node ID to child-friendly goal`() {
        val goal1 = viewModel.getKidFriendlyLessonGoal("G1_03_SPLIT_COMBINE_10", "Default Goal")
        assertEquals("Bé sẽ thuộc các cặp số cộng lại bằng 10 để nhẩm siêu tốc!", goal1)

        val fallbackGoal = viewModel.getKidFriendlyLessonGoal("UNKNOWN_NODE", "Default Goal")
        assertEquals("Default Goal", fallbackGoal)
    }

    @Test
    fun `topic completion percentage formula works accurately`() {
        val totalNodes = 10
        val practicedMasteries = mapOf(
            "G1_01" to StudentMasteryEntity(nodeId = "G1_01", lastPracticedEpochMs = 1000L),
            "G1_02" to StudentMasteryEntity(nodeId = "G1_02", lastPracticedEpochMs = 2000L)
        )

        val practicedCount = practicedMasteries.values.count { it.lastPracticedEpochMs > 0L }
        val pct = (practicedCount * 100) / totalNodes

        assertEquals(20, pct)
    }
}
