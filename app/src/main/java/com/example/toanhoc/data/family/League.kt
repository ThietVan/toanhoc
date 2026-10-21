package com.example.toanhoc.data.family

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

/* ------------------------------------------------------------------------------------------
 * GIẢI ĐẤU (bản mô phỏng – trước khi có thi đấu trực tuyến thật)
 *
 * - Mỗi tuần con ở trong một bảng 5 người: con + 4 bạn thi mô phỏng (tên, avatar như bạn cùng lớp).
 *   Bảng đổi bạn mới mỗi tuần (thứ Hai).
 * - Mỗi trận 10 câu khó; xếp hạng theo số câu đúng, bằng nhau thì ai nhanh hơn.
 * - Bạn thi mô phỏng làm bài như học sinh thật: xác suất đúng quanh mức trung bình (~68%),
 *   câu khó dễ sai hơn, thời gian mỗi câu dao động. Mức này tự chỉnh nhẹ theo kết quả của con
 *   để con về nhất khoảng 40–50% số trận (top 2 khoảng 65–70%).
 * - Hạng giải: Đồng → Bạc → Vàng → Kim cương → Huyền thoại. Cuối tuần top 2 lên hạng (lên nhanh);
 *   chỉ xuống hạng khi đứng cuối VÀ chơi ít hơn 2 trận trong tuần (tụt nhẹ).
 * - Phí 20 xu/trận, 1 vé miễn phí mỗi ngày, tối đa 5 trận/ngày.
 * ---------------------------------------------------------------------------------------- */

@Serializable
data class LeagueBot(
    val id: String,
    val name: String,
    val gender: String = "trai",
    val skin: String = "s1",
    val hair: String = "ngan",
    val hairColor: String = "den",
    val outfit: String = "aothun",
    val accessory: String = "none",
    val scene: String = "phong",
    /** Cộng/trừ vào mức đúng chung → tính cách: cẩn thận, ẩu… */
    val skillOffset: Float,
    /** Nhân thời gian làm mỗi câu (<1 = nhanh). */
    val speedFactor: Float,
    /** Điểm giải trung bình mỗi ngày. */
    val pace: Float,
    val seed: Long
) {
    val look: Look get() = Look(gender, skin, hair, hairColor, outfit, scene = scene, accessory = accessory)
}

@Serializable
data class LeagueWeekResult(val fromTier: Int, val toTier: Int, val rank: Int, val seen: Boolean = false)

@Serializable
data class LeagueState(
    val weekStart: String = "",
    val tier: Int = 0,
    val points: Int = 0,
    val bots: List<LeagueBot> = emptyList(),
    val matchesThisWeek: Int = 0,
    /** Mức đúng trung bình của bạn thi (tự chỉnh theo con). */
    val rating: Float = League.BASE_RATING,
    val day: String = "",
    val matchesToday: Int = 0,
    val freeUsedToday: Boolean = false,
    val played: Int = 0,
    val wins: Int = 0,
    val correct: Int = 0,
    val answered: Int = 0,
    val lastWeek: LeagueWeekResult? = null
)

data class LeagueTier(val name: String, val emoji: String, val color: Long, val soft: Long)

data class Standing(val name: String, val isMe: Boolean, val points: Int, val bot: LeagueBot?)

/** Một bạn thi trong trận: đúng/sai từng câu và thời điểm (ms) xong từng câu, cộng dồn. */
data class BotRun(val bot: LeagueBot, val correct: List<Boolean>, val doneAtMs: List<Long>) {
    val totalCorrect get() = correct.count { it }
    val totalMs get() = doneAtMs.lastOrNull() ?: 0L
}

data class MatchResult(val name: String, val isMe: Boolean, val correct: Int, val timeMs: Long, val bot: LeagueBot?)

enum class EntryKind { FREE, PAID, NO_COINS, DAY_LIMIT }

object League {
    const val BASE_RATING = 0.68f
    const val FEE = 20
    const val MAX_PER_DAY = 5
    const val QUESTIONS = 10
    /** Xu nhận về theo hạng trong trận (phí 20): hạng 3 hòa vốn. */
    val PAYOUT = listOf(40, 25, 20, 5, 5)
    private val PLACE_POINTS = listOf(30, 20, 10, 5, 0)

    val TIERS = listOf(
        LeagueTier("Đồng", "🥉", 0xFFC27C46, 0xFFF7E6D7),
        LeagueTier("Bạc", "🥈", 0xFF8E9AAB, 0xFFEEF2F7),
        LeagueTier("Vàng", "🥇", 0xFFE0A100, 0xFFFFF3C4),
        LeagueTier("Kim cương", "💎", 0xFF3B82F6, 0xFFE3EEFF),
        LeagueTier("Huyền thoại", "👑", 0xFF8B5CF6, 0xFFF0E9FF)
    )

    val NAMES = listOf(
        "Minh Khôi", "Bảo An", "Gia Huy", "Ngọc Hân", "Tuệ Lâm", "Đức Anh", "Khánh Linh", "Hải Đăng",
        "Phương Vy", "Quang Minh", "Thảo My", "Nhật Nam", "An Nhiên", "Tuấn Kiệt", "Mai Chi", "Bảo Ngọc",
        "Hoàng Long", "Diệu Anh", "Thiên Phúc", "Minh Thư", "Gia Bảo", "Hà My", "Đăng Khoa", "Yến Nhi"
    )

    private val GIRL_NAMES = setOf("Bảo An", "Ngọc Hân", "Khánh Linh", "Phương Vy", "Thảo My", "An Nhiên", "Mai Chi", "Bảo Ngọc", "Diệu Anh", "Minh Thư", "Hà My", "Yến Nhi")

    /** Bốn kiểu bạn thi: nhanh mà ẩu · chậm mà chắc · đều tay · thất thường. */
    private val STYLES = listOf(-0.08f to 0.75f, 0.06f to 1.30f, 0.0f to 1.0f, -0.03f to 0.95f)

    fun weekStart(d: LocalDate): LocalDate = d.with(DayOfWeek.MONDAY)

    fun makeBots(childId: String, weekStart: LocalDate, tier: Int): List<LeagueBot> {
        val rng = Random((childId + weekStart).hashCode().toLong() * 31 + tier)
        val names = NAMES.shuffled(rng).take(4)
        val skins = AvatarShop.of(ShopKind.SKIN).map { it.id }
        val colors = AvatarShop.of(ShopKind.HAIR_COLOR)
        val outfits = AvatarShop.of(ShopKind.OUTFIT)
        val accs = AvatarShop.of(ShopKind.ACCESSORY)
        val basePace = 110f * (1f + 0.25f * tier)
        return names.mapIndexed { i, n ->
            val (skill, speed) = STYLES[i]
            LeagueBot(
                id = "b$i", name = n,
                gender = if (n in GIRL_NAMES) "gai" else "trai",
                skin = skins[rng.nextInt(skins.size)],
                hair = if (n in GIRL_NAMES) listOf("duoingua", "haibui", "dai", "ngan")[rng.nextInt(4)] else listOf("ngan", "ngoi", "dung")[rng.nextInt(3)],
                // hạng càng cao, bạn thi càng "chịu chơi" đồ đẹp
                hairColor = colors.filter { it.price <= 40 * (tier + 1) }.let { it[rng.nextInt(it.size)].id },
                scene = AvatarShop.of(ShopKind.SCENE)[rng.nextInt(AvatarShop.of(ShopKind.SCENE).size)].id,
                // hạng càng cao, bạn thi càng hay đeo phụ kiện đẹp
                accessory = if (rng.nextFloat() < 0.2f + 0.15f * tier) accs.filter { it.id != "none" && it.price <= 60 * (tier + 1) }.let { if (it.isEmpty()) "none" else it[rng.nextInt(it.size)].id } else "none",
                outfit = outfits.filter { it.price <= 60 * (tier + 1) && (it.id != "vay" || n in GIRL_NAMES) }.let { it[rng.nextInt(it.size)].id },
                skillOffset = skill + (rng.nextFloat() - 0.5f) * 0.04f,
                speedFactor = speed * (0.9f + rng.nextFloat() * 0.2f),
                pace = basePace * (0.6f + rng.nextFloat() * 0.8f),
                seed = rng.nextLong()
            )
        }.shuffled(rng)
    }

    /** Điểm giải của bạn thi tại thời điểm [now]: tăng dần theo từng ngày như đang chơi thật. */
    fun botPoints(bot: LeagueBot, weekStart: LocalDate, now: LocalDateTime): Int {
        val hours = ChronoUnit.MINUTES.between(weekStart.atStartOfDay(), now).coerceIn(0, 7L * 24 * 60) / 60.0
        val fullDays = (hours / 24).toInt()
        var pts = 0.0
        for (d in 0 until fullDays.coerceAtMost(7)) pts += dayPoints(bot, d)
        if (fullDays < 7) {
            // trong ngày: phần lớn điểm dồn vào buổi chiều tối
            val frac = ((hours - fullDays * 24) / 24).coerceIn(0.0, 1.0)
            val curve = when {
                frac < 0.3 -> frac * 0.2
                frac < 0.7 -> 0.06 + (frac - 0.3) * 0.9
                else -> 0.42 + (frac - 0.7) * 1.93
            }.coerceAtMost(1.0)
            pts += dayPoints(bot, fullDays) * curve
        }
        return pts.roundToInt()
    }

    private fun dayPoints(bot: LeagueBot, day: Int): Double {
        val r = Random(bot.seed + day * 7919L)
        if (r.nextFloat() < 0.15f) return 0.0 // có ngày bạn ấy nghỉ
        return bot.pace * (0.3 + r.nextDouble() * 1.3)
    }

    fun standings(state: LeagueState, myName: String, now: LocalDateTime): List<Standing> {
        val ws = runCatching { LocalDate.parse(state.weekStart) }.getOrNull() ?: weekStart(now.toLocalDate())
        return (state.bots.map { Standing(it.name, false, botPoints(it, ws, now), it) } + Standing(myName, true, state.points, null))
            .sortedWith(compareByDescending<Standing> { it.points }.thenBy { if (it.isMe) 0 else 1 })
    }

    /** Sang tuần mới / ngày mới: chốt hạng tuần cũ, đổi bạn, đặt lại lượt trong ngày. */
    fun roll(state: LeagueState, childId: String, now: LocalDateTime): LeagueState {
        val today = now.toLocalDate()
        val ws = weekStart(today)
        var s = state
        if (s.weekStart.isEmpty()) {
            s = s.copy(weekStart = ws.toString(), bots = makeBots(childId, ws, s.tier))
        } else if (s.weekStart != ws.toString()) {
            val oldWs = LocalDate.parse(s.weekStart)
            val end = oldWs.plusDays(7).atStartOfDay()
            val rank = standings(s, "me", end).indexOfFirst { it.isMe } + 1
            val played = s.matchesThisWeek
            val newTier = when {
                played == 0 -> s.tier // không chơi tuần đó: giữ nguyên hạng
                rank <= 2 -> (s.tier + 1).coerceAtMost(TIERS.lastIndex)
                rank == 5 && played < 2 -> (s.tier - 1).coerceAtLeast(0)
                else -> s.tier
            }
            s = s.copy(
                weekStart = ws.toString(), tier = newTier, points = 0, matchesThisWeek = 0,
                bots = makeBots(childId, ws, newTier),
                lastWeek = if (played > 0) LeagueWeekResult(s.tier, newTier, rank) else s.lastWeek?.copy(seen = true)
            )
        }
        if (s.day != today.toString()) s = s.copy(day = today.toString(), matchesToday = 0, freeUsedToday = false)
        return s
    }

    fun entry(state: LeagueState, coins: Int): EntryKind = when {
        state.matchesToday >= MAX_PER_DAY -> EntryKind.DAY_LIMIT
        !state.freeUsedToday -> EntryKind.FREE
        coins >= FEE -> EntryKind.PAID
        else -> EntryKind.NO_COINS
    }

    /** Thời gian trung bình mỗi câu (giây) của học sinh trung bình theo lớp. */
    fun baseSeconds(grade: Int) = 9.0 + grade * 1.2

    /** Mô phỏng một bạn thi làm [hard].size câu. */
    fun simulate(bot: LeagueBot, rating: Float, hard: List<Boolean>, grade: Int, rng: Random): BotRun {
        var t = 0L
        val correct = ArrayList<Boolean>()
        val times = ArrayList<Long>()
        hard.forEach { h ->
            val p = (rating + bot.skillOffset - if (h) 0.07f else 0f).coerceIn(0.25f, 0.96f)
            val ok = rng.nextFloat() < p
            // thời gian: dao động 0,6–1,6 lần; câu khó lâu hơn; câu sai thường do vội nên nhanh hơn chút
            var sec = baseSeconds(grade) * bot.speedFactor * (0.6 + rng.nextDouble()) * (if (h) 1.25 else 1.0)
            if (!ok) sec *= 0.85
            if (rng.nextFloat() < 0.08f) sec *= 1.8 // thỉnh thoảng "ngập ngừng" ở một câu
            t += (sec * 1000).toLong()
            correct += ok
            times += t
        }
        return BotRun(bot, correct, times)
    }

    fun rank(results: List<MatchResult>): List<MatchResult> =
        results.sortedWith(compareByDescending<MatchResult> { it.correct }.thenBy { it.timeMs })

    /** Điểm giải của trận: 10 điểm/câu đúng + điểm hạng + thưởng nhanh (tối đa 20). */
    fun matchPoints(correct: Int, place: Int, timeMs: Long, grade: Int): Int {
        val limit = QUESTIONS * baseSeconds(grade) * 1.5
        val speed = (20 * (1 - timeMs / 1000.0 / limit)).coerceIn(0.0, 20.0)
        return correct * 10 + PLACE_POINTS[(place - 1).coerceIn(0, 4)] + if (correct >= 5) speed.roundToInt() else 0
    }

    /** Chỉnh mức của bạn thi để con về nhất khoảng 40–50% số trận. */
    fun adapt(rating: Float, place: Int): Float = (rating + when (place) {
        1 -> 0.025f; 2 -> -0.01f; 3 -> -0.02f; 4 -> -0.03f; else -> -0.04f
    }).coerceIn(0.40f, 0.90f)
}
