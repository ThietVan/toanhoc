package com.example.toanhoc.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toanhoc.data.family.BadgeDef
import com.example.toanhoc.data.family.Badges
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.League
import androidx.compose.runtime.LaunchedEffect
import java.time.LocalDateTime
import com.example.toanhoc.ui.family.ShopScreen
import com.example.toanhoc.data.family.FamilyData
import com.example.toanhoc.data.family.Reward
import com.example.toanhoc.data.family.ShopItem
import com.example.toanhoc.data.local.entity.StudentProfileEntity
import com.example.toanhoc.ui.achievements.AchievementsScreen
import com.example.toanhoc.ui.family.ChildProfileScreen
import com.example.toanhoc.ui.family.RestTimeScreen
import com.example.toanhoc.ui.journey.LessonJourneyScreen
import com.example.toanhoc.ui.navigation.MainBottomNavigationBar
import com.example.toanhoc.ui.profile.UserProfileScreen
import com.example.toanhoc.ui.tournament.TournamentScreen

/** Hồ sơ con đang học + thao tác của con (xu, trang trí, đổi quà, đổi người học). */
class ChildHub(
    val data: FamilyData,
    val child: ChildProfile,
    /** Số phút còn được học hôm nay (null = không giới hạn). */
    val minutesLeft: Int?,
    val onBuy: (ShopItem) -> Unit,
    val onEquip: (ShopItem) -> Unit,
    val onRequestReward: (Reward) -> Unit,
    val onClaimBadge: (BadgeDef, Int) -> Unit,
    val onOpenLeague: () -> Unit,
    val onSetGender: (String) -> Unit,
    val onPlayLeague: () -> Unit,
    val onAckLeagueWeek: () -> Unit,
    val onSwitchProfile: () -> Unit,
    val onAskMoreTime: () -> Unit,
    /** Thử thách vượt bài bố mẹ đã giao (chưa làm xong). */
    val challenge: com.example.toanhoc.domain.challenge.Challenge? = null,
    val onStartChallenge: () -> Unit = {}
)

/**
 * Main Academic Dashboard Screen integrating 4-Tab Bottom Navigation
 * (Bài học, Thành tích, Giải đấu, Cá nhân).
 */
@Composable
fun AcademicDashboardScreen(
    uiState: DashboardUiState,
    studentProfile: StudentProfileEntity,
    onSelectGrade: (Int) -> Unit,
    onStartSession: (String?) -> Unit,
    onStartLevelUpExam: () -> Unit,
    onStartParentExam: () -> Unit,
    onUpdateProfile: (StudentProfileEntity) -> Unit,
    modifier: Modifier = Modifier,
    lessonProgress: Map<String, Int> = emptyMap(),
    onOpenLesson: (String) -> Unit = {},
    onOpenExam: (Int) -> Unit = {},
    hub: ChildHub? = null,
    /** Tab đang chọn do MainActivity giữ (để quay lại đúng tab sau khi thi đấu). */
    tab: Int? = null,
    onTabChange: (Int) -> Unit = {}
) {
    var localTab by remember { mutableIntStateOf(0) } // 0=Bài học, 1=Cửa hàng, 2=Giải đấu, 3=Cá nhân
    val selectedBottomTab = tab ?: localTab

    // Lời chào và chuỗi ngày lấy theo hồ sơ con đang học
    val profile = if (hub != null) studentProfile.copy(fullName = hub.child.name, streakDays = hub.data.streak(hub.child.id)) else studentProfile

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedBottomTab) {
                0 -> {
                    if (hub != null && hub.minutesLeft == 0) {
                        RestTimeScreen(
                            child = hub.child,
                            limitMinutes = hub.child.dailyLimitMinutes,
                            onAskParent = hub.onAskMoreTime,
                            onSwitch = hub.onSwitchProfile
                        )
                    } else {
                        LessonJourneyScreen(
                            uiState = uiState,
                            studentProfile = profile,
                            onStartSession = onStartSession,
                            onStartLevelUpExam = onStartLevelUpExam,
                            lessonProgress = lessonProgress,
                            onOpenLesson = onOpenLesson,
                            onOpenExam = onOpenExam,
                            coins = hub?.child?.coins,
                            topBanner = hub?.challenge?.let { ch ->
                                { com.example.toanhoc.ui.family.ChallengeBanner(ch, hub.onStartChallenge) }
                            }
                        )
                    }
                }
                1 -> {
                    if (hub != null) {
                        ShopScreen(
                            data = hub.data,
                            child = hub.child,
                            onBuy = hub.onBuy,
                            onEquip = hub.onEquip,
                            onRequestReward = hub.onRequestReward,
                            onSetGender = hub.onSetGender
                        )
                    } else {
                        AchievementsScreen(uiState = uiState, studentProfile = profile)
                    }
                }
                2 -> {
                    if (hub != null) {
                        LaunchedEffect(hub.child.id) { hub.onOpenLeague() }
                        TournamentScreen(
                            child = hub.child,
                            league = League.roll(hub.child.league, hub.child.id, LocalDateTime.now()),
                            onPlay = hub.onPlayLeague,
                            onAckWeek = hub.onAckLeagueWeek
                        )
                    }
                }
                3 -> {
                    if (hub != null) {
                        ChildProfileScreen(
                            data = hub.data,
                            child = hub.child,
                            onClaimBadge = hub.onClaimBadge,
                            onSwitchProfile = hub.onSwitchProfile,
                            onOpenShop = { localTab = 1; onTabChange(1) }
                        )
                    } else {
                        UserProfileScreen(
                            profile = studentProfile,
                            misconceptionLogs = uiState.misconceptionLogs,
                            onSelectGrade = onSelectGrade,
                            onUpdateProfile = onUpdateProfile,
                            onStartLevelUpExam = onStartLevelUpExam,
                            onStartParentExam = onStartParentExam
                        )
                    }
                }
            }
        }

        // Chấm đỏ: Cửa hàng khi đủ xu đổi một món quà, Cá nhân khi có huy hiệu chờ nhận
        val dots = buildSet {
            if (hub != null) {
                if (hub.data.rewardsFor(hub.child.id).any { hub.child.coins >= it.cost }) add(1)
                if (Badges.claimable(hub.child, hub.data).isNotEmpty()) add(3)
            }
        }
        MainBottomNavigationBar(
            selectedTab = selectedBottomTab,
            onTabSelected = { localTab = it; onTabChange(it) },
            dots = dots,
            secondTab = if (hub != null) "Cửa hàng" else null
        )
    }
}
