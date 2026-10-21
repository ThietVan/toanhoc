package com.example.toanhoc.data.family

import kotlinx.serialization.Serializable
import java.time.LocalDate

/* ------------------------------------------------------------------------------------------
 * Mô hình dữ liệu gia đình: nhiều hồ sơ con, ví xu, đồ trang trí, quà bố mẹ cài,
 * nhật ký học (phút/ngày) và nhật ký câu sai để làm báo cáo cho phụ huynh.
 * Toàn bộ lưu trên máy, chỉ giữ tên + năm sinh của con (không ảnh, không liên hệ).
 * ---------------------------------------------------------------------------------------- */

@Serializable
data class ChildProfile(
    val id: String,
    val name: String,
    val birthYear: Int,
    val grade: Int = 1,
    /* Nhân vật: bé trai/bé gái + các lớp trang phục (id trong [AvatarShop]). */
    val gender: String = "trai",
    val skin: String = "s1",
    val hair: String = "ngan",
    val hairColor: String = "den",
    val outfit: String = "dongphuc",
    val scene: String = "phong",
    val pet: String = "none",
    val accessory: String = "none",
    /** 0 = hồ sơ từ bản avatar con vật cũ (cần chuyển đổi), 2 = nhân vật mới. */
    val artVersion: Int = 0,
    val coins: Int = 0,
    val owned: Set<String> = emptySet(),
    /** Giới hạn phút học mỗi ngày (0 = không giới hạn). */
    val dailyLimitMinutes: Int = 0,
    /** Phút bố mẹ cho thêm trong ngày: ngày → phút. */
    val bonusMinutes: Map<String, Int> = emptyMap(),
    /** Các cấp huy hiệu con đã bấm nhận xu: "streak:0", "streak:1"… */
    val claimed: Set<String> = emptySet(),
    /** Bộ đếm thành tích: hardPassed, lessonsFinished, relearnDone, perfectRound, examPassed, bestStreak. */
    val counters: Map<String, Int> = emptyMap(),
    val league: LeagueState = LeagueState(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun counter(key: String): Int = counters[key] ?: 0
    val look: Look get() = Look(gender, skin, hair, hairColor, outfit, scene, pet, accessory)

    val age: Int get() = LocalDate.now().year - birthYear
    /** Lớp theo độ tuổi ở Việt Nam: vào lớp 1 năm tròn 6 tuổi. */
    val gradeByAge: Int get() = (age - 5).coerceIn(1, 5)
}

@Serializable
data class Reward(
    val id: String,
    val title: String,
    val emoji: String,
    val cost: Int,
    /** null = dành cho mọi con; khác null = chỉ con này. */
    val childId: String? = null
)

@Serializable
data class RewardRequest(
    val id: String,
    val childId: String,
    val rewardId: String,
    val title: String,
    val emoji: String,
    val cost: Int,
    val requestedAt: Long,
    val deliveredAt: Long? = null
) {
    val delivered: Boolean get() = deliveredAt != null
}

@Serializable
data class CoinEvent(val childId: String, val at: Long, val amount: Int, val reason: String)

@Serializable
data class MistakeEntry(
    val childId: String,
    val at: Long,
    /** Bài trong sách (L2_B12…) – null khi luyện theo nhóm kiến thức cũ. */
    val lessonId: String?,
    val nodeId: String,
    val statement: String,
    val picked: String,
    val expected: String,
    val source: String = "practice" // practice | exam
)

@Serializable
data class FamilyData(
    val children: List<ChildProfile> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val requests: List<RewardRequest> = emptyList(),
    val coinLog: List<CoinEvent> = emptyList(),
    val mistakes: List<MistakeEntry> = emptyList(),
    /** childId → (yyyy-MM-dd → giây học). */
    val studySeconds: Map<String, Map<String, Int>> = emptyMap(),
    /** childId → các ngày đã nhận thưởng "học hôm nay" (tránh cộng lặp). */
    val dailyBonusDays: Map<String, Set<String>> = emptyMap(),
    /** Thử thách vượt bài (lộ trình do bố mẹ chọn), mới nhất ở cuối. */
    val challenges: List<com.example.toanhoc.domain.challenge.Challenge> = emptyList(),
    val nextId: Int = 1
) {
    /** Thử thách đang chờ con làm (chưa xong). */
    fun pendingChallenge(childId: String?): com.example.toanhoc.domain.challenge.Challenge? =
        challenges.lastOrNull { it.childId == childId && !it.finished }

    fun challengesOf(childId: String): List<com.example.toanhoc.domain.challenge.Challenge> =
        challenges.filter { it.childId == childId }

    fun child(id: String?): ChildProfile? = children.firstOrNull { it.id == id }

    fun todayMinutes(childId: String, today: String = LocalDate.now().toString()): Int =
        (studySeconds[childId]?.get(today) ?: 0) / 60

    /** Phút học 7 ngày gần nhất, cũ → mới. */
    fun last7Days(childId: String, today: LocalDate = LocalDate.now()): List<Pair<LocalDate, Int>> =
        (6 downTo 0).map { d ->
            val day = today.minusDays(d.toLong())
            day to ((studySeconds[childId]?.get(day.toString()) ?: 0) / 60)
        }

    /** Chuỗi ngày học liên tiếp (ngày học ≥ 1 phút), tính đến hôm nay hoặc hôm qua. */
    fun streak(childId: String, today: LocalDate = LocalDate.now()): Int {
        val days = studySeconds[childId].orEmpty()
        fun studied(d: LocalDate) = (days[d.toString()] ?: 0) >= 60
        var d = if (studied(today)) today else today.minusDays(1)
        var n = 0
        while (studied(d)) { n++; d = d.minusDays(1) }
        return n
    }

    fun rewardsFor(childId: String): List<Reward> = rewards.filter { it.childId == null || it.childId == childId }
}

/* ------------------------------------- Quy tắc xu ------------------------------------- */

/**
 * Xu thưởng cho sự THÀNH THẠO và CHĂM CHỈ, không thưởng theo số câu:
 *  - chỉ cộng khi lần đầu qua một bước của bài (làm lại bước cũ không có xu → không "cày" được);
 *  - xu không dùng để bỏ qua hay mở khóa bài.
 * Ước lượng: học đều ~2 bước/ngày ≈ 30 xu/ngày ≈ 200 xu/tuần.
 */
object CoinRules {
    const val LEARN = 5
    const val EASY = 10
    const val HARD = 15
    const val REVIEW = 10
    const val RELEARN_DONE = 20
    const val EXAM_PASS = 100
    const val DAILY = 5

    /** Giá quà gợi ý cho bố mẹ: nhỏ ≈ 1 tuần, vừa ≈ 2–3 tuần, lớn ≈ 1–1,5 tháng học đều. */
    val SUGGESTED_COSTS = listOf(150 to "Quà nhỏ · ~1 tuần", 400 to "Quà vừa · ~2–3 tuần", 1000 to "Quà lớn · ~1 tháng")

    val EXPLAIN = listOf(
        "Xem xong Bài giảng lần đầu" to LEARN,
        "Qua Dạng 1 lần đầu" to EASY,
        "Qua Dạng 2 lần đầu" to HARD,
        "Qua Ôn tập lần đầu" to REVIEW,
        "Học lại xong một bài bị trả về" to RELEARN_DONE,
        "Đạt bài kiểm tra học kì" to EXAM_PASS,
        "Mỗi ngày có học" to DAILY
    )
}

/* ---------------------------------- Nhân vật & cửa hàng ---------------------------------- */

/** Toàn bộ "bộ đồ" của một nhân vật (con hoặc bạn thi) – dùng để vẽ. */
data class Look(
    val gender: String = "trai",
    val skin: String = "s1",
    val hair: String = "ngan",
    val hairColor: String = "den",
    val outfit: String = "dongphuc",
    val scene: String = "phong",
    val pet: String = "none",
    val accessory: String = "none"
) {
    fun with(item: ShopItem): Look = when (item.kind) {
        ShopKind.ACCESSORY -> copy(accessory = item.id)
        ShopKind.SKIN -> copy(skin = item.id)
        ShopKind.HAIR -> copy(hair = item.id)
        ShopKind.HAIR_COLOR -> copy(hairColor = item.id)
        ShopKind.OUTFIT -> copy(outfit = item.id)
        ShopKind.SCENE -> copy(scene = item.id)
        ShopKind.PET -> copy(pet = item.id)
    }
}

enum class ShopKind(val label: String) {
    SKIN("Màu da"), HAIR("Kiểu tóc"), HAIR_COLOR("Màu tóc"), OUTFIT("Trang phục"), SCENE("Phông nền"), PET("Thú cưng"), ACCESSORY("Phụ kiện")
}

/** [color] = màu ô xem nhanh (màu da, màu tóc, màu chính của bộ đồ/phông/thú). */
data class ShopItem(val id: String, val kind: ShopKind, val name: String, val price: Int, val color: Long = 0xFFE4E7EC, val color2: Long = 0)

object AvatarShop {
    val items: List<ShopItem> = listOf(
        ShopItem("s1", ShopKind.SKIN, "Da sáng", 0, 0xFFFFE0C7),
        ShopItem("s2", ShopKind.SKIN, "Da vừa", 0, 0xFFF2C39A),
        ShopItem("s3", ShopKind.SKIN, "Da ngăm", 0, 0xFFD29A6A),
        ShopItem("s4", ShopKind.SKIN, "Da nâu", 0, 0xFF9C6B45),

        ShopItem("ngan", ShopKind.HAIR, "Tóc ngắn", 0),
        ShopItem("duoingua", ShopKind.HAIR, "Đuôi ngựa", 0),
        ShopItem("ngoi", ShopKind.HAIR, "Ngôi lệch", 30),
        ShopItem("dung", ShopKind.HAIR, "Tóc dựng", 40),
        ShopItem("haibui", ShopKind.HAIR, "Hai búi", 50),
        ShopItem("dai", ShopKind.HAIR, "Tóc dài", 60),

        ShopItem("den", ShopKind.HAIR_COLOR, "Đen", 0, 0xFF2B2220),
        ShopItem("nau", ShopKind.HAIR_COLOR, "Nâu", 0, 0xFF6B3F24),
        ShopItem("vang", ShopKind.HAIR_COLOR, "Vàng", 40, 0xFFE3A93B),
        ShopItem("hong", ShopKind.HAIR_COLOR, "Hồng", 80, 0xFFF27BA3),
        ShopItem("xanh", ShopKind.HAIR_COLOR, "Xanh", 80, 0xFF4F7BF0),

        ShopItem("dongphuc", ShopKind.OUTFIT, "Đồng phục", 0, 0xFFFFFFFF, 0xFF22315C),
        ShopItem("aothun", ShopKind.OUTFIT, "Áo thun", 0, 0xFF4F7BF0, 0xFF2B3A67),
        ShopItem("hoodie", ShopKind.OUTFIT, "Hoodie", 80, 0xFFF28A3D, 0xFF3A3F4B),
        ShopItem("vay", ShopKind.OUTFIT, "Váy hồng", 80, 0xFFF27BA3, 0xFFF7A6C2),
        ShopItem("aodai", ShopKind.OUTFIT, "Áo dài", 150, 0xFFF6C33B, 0xFFFFFFFF),
        ShopItem("sieunhan", ShopKind.OUTFIT, "Siêu nhân", 200, 0xFFE5484D, 0xFF2F55D4),
        ShopItem("phihanhgia", ShopKind.OUTFIT, "Phi hành gia", 250, 0xFFF4F6FA, 0xFF8EA2C4),

        ShopItem("phong", ShopKind.SCENE, "Phòng ngủ", 0, 0xFFFCE9D2, 0xFFE5C49B),
        ShopItem("congvien", ShopKind.SCENE, "Công viên", 100, 0xFFCFEBFF, 0xFF8FD19E),
        ShopItem("lop", ShopKind.SCENE, "Lớp học", 120, 0xFF2F6B4F, 0xFFEAF4EE),
        ShopItem("bien", ShopKind.SCENE, "Bãi biển", 200, 0xFFBDE6FF, 0xFFF6DFA6),
        ShopItem("vutru", ShopKind.SCENE, "Vũ trụ", 300, 0xFF1B1F4B, 0xFF8B5CF6),

        ShopItem("none", ShopKind.ACCESSORY, "Không", 0, 0xFFF3F4F6),
        ShopItem("no", ShopKind.ACCESSORY, "Nơ hồng", 50, 0xFFF27BA3),
        ShopItem("kinh", ShopKind.ACCESSORY, "Kính tròn", 60, 0xFF2B2220),
        ShopItem("mu", ShopKind.ACCESSORY, "Mũ sinh nhật", 60, 0xFF7C3AED),
        ShopItem("kinhram", ShopKind.ACCESSORY, "Kính râm", 80, 0xFF111827),
        ShopItem("huychuong", ShopKind.ACCESSORY, "Huy chương", 120, 0xFFFFC21A),
        ShopItem("vuongmien", ShopKind.ACCESSORY, "Vương miện", 200, 0xFFE0A100),

        ShopItem("none", ShopKind.PET, "Không", 0, 0xFFF3F4F6),
        ShopItem("meo", ShopKind.PET, "Mèo cam", 300, 0xFFF2A65A),
        ShopItem("cun", ShopKind.PET, "Cún nâu", 300, 0xFFC58B5C),
        ShopItem("tho", ShopKind.PET, "Thỏ bông", 350, 0xFFF7F7F7),
        ShopItem("rong", ShopKind.PET, "Rồng con", 600, 0xFF4FBF7A)
    )

    /** Nhóm hiển thị trong cửa hàng: (tên nhóm, các loại đồ). */
    val TABS: List<Pair<String, List<ShopKind>>> = listOf(
        "Phụ kiện" to listOf(ShopKind.ACCESSORY),
        "Phông nền" to listOf(ShopKind.SCENE),
        "Thú cưng" to listOf(ShopKind.PET)
    )

    fun key(item: ShopItem) = "${item.kind.name}:${item.id}"
    fun of(kind: ShopKind) = items.filter { it.kind == kind }
    fun find(kind: ShopKind, id: String) = items.firstOrNull { it.kind == kind && it.id == id }

    fun isOwned(child: ChildProfile, item: ShopItem) = item.price == 0 || key(item) in child.owned
    fun isEquipped(child: ChildProfile, item: ShopItem) = when (item.kind) {
        ShopKind.SKIN -> child.skin == item.id
        ShopKind.HAIR -> child.hair == item.id
        ShopKind.HAIR_COLOR -> child.hairColor == item.id
        ShopKind.OUTFIT -> child.outfit == item.id
        ShopKind.SCENE -> child.scene == item.id
        ShopKind.PET -> child.pet == item.id
        ShopKind.ACCESSORY -> child.accessory == item.id
    }

    /** Kiểu tóc mặc định theo bé trai / bé gái. */
    fun defaultHair(gender: String) = if (gender == "gai") "duoingua" else "ngan"

    /** Giá đồ avatar con vật cũ (để hoàn xu khi chuyển sang nhân vật mới). */
    val LEGACY_PRICES = mapOf(
        "ANIMAL:fox" to 60, "ANIMAL:panda" to 80, "ANIMAL:koala" to 80, "ANIMAL:tiger" to 120, "ANIMAL:lion" to 120,
        "ANIMAL:penguin" to 150, "ANIMAL:owl" to 150, "ANIMAL:unicorn" to 250, "ANIMAL:dragon" to 300,
        "BACKGROUND:peach" to 30, "BACKGROUND:lemon" to 30, "BACKGROUND:lavender" to 40, "BACKGROUND:sunset" to 80,
        "BACKGROUND:ocean" to 80, "BACKGROUND:galaxy" to 150,
        "FRAME:silver" to 50, "FRAME:gold" to 120, "FRAME:rainbow" to 200,
        "ACCESSORY:star" to 40, "ACCESSORY:bow" to 50, "ACCESSORY:glasses" to 60, "ACCESSORY:cap" to 100,
        "ACCESSORY:rocket" to 120, "ACCESSORY:crown" to 200
    )

    val REWARD_EMOJIS = listOf("🎁", "🍦", "🧸", "📚", "🎮", "🎡", "🍕", "🚲", "🎨", "⚽", "🎬", "🏊")
}

/* ------------------------------------- Huy hiệu ------------------------------------- */

enum class BadgeTier(val label: String, val ring: Long, val fill: Long) {
    BRONZE("Đồng", 0xFFC27C46, 0xFFF7E6D7),
    SILVER("Bạc", 0xFF8E9AAB, 0xFFEEF2F7),
    GOLD("Vàng", 0xFFE0A100, 0xFFFFF3C4)
}

/**
 * Một loại huy hiệu có 3 cấp Đồng – Bạc – Vàng. Đạt mốc → con bấm "Nhận" để lấy xu.
 * [goal] mô tả mốc bằng lời cho trẻ; [unit] đơn vị hiển thị tiến độ.
 */
class BadgeDef(
    val id: String,
    val emoji: String,
    val name: String,
    val unit: String,
    val targets: List<Int>,
    val coins: List<Int>,
    val goal: (Int) -> String,
    val metric: (ChildProfile, FamilyData) -> Int
) {
    fun key(tier: Int) = "$id:$tier"
    /** Số cấp đã đạt (0..3). */
    fun reached(value: Int) = targets.count { value >= it }
}

object Badges {
    object Keys {
        const val HARD = "hardPassed"
        const val LESSONS = "lessonsFinished"
        const val RELEARN = "relearnDone"
        const val PERFECT = "perfectRound"
        const val EXAM = "examPassed"
        const val BEST_STREAK = "bestStreak"
        const val LEAGUE_WINS = "leagueWins"
    }

    val all: List<BadgeDef> = listOf(
        BadgeDef("streak", "🔥", "Chăm chỉ", "ngày", listOf(3, 7, 30), listOf(10, 30, 100),
            { "Học $it ngày liên tiếp" }) { c, d -> maxOf(c.counter(Keys.BEST_STREAK), d.streak(c.id)) },
        BadgeDef("minutes", "⏱️", "Bền bỉ", "phút", listOf(60, 300, 1000), listOf(10, 40, 100),
            { "Học tổng cộng $it phút" }) { c, d -> (d.studySeconds[c.id]?.values?.sum() ?: 0) / 60 },
        BadgeDef("lessons", "📘", "Mọt sách", "bài", listOf(1, 10, 30), listOf(10, 40, 100),
            { "Học trọn $it bài (đủ 4 bước)" }) { c, _ -> c.counter(Keys.LESSONS) },
        BadgeDef("hard", "💪", "Không ngại khó", "bài", listOf(3, 15, 40), listOf(15, 40, 100),
            { "Qua Dạng 2 (khó) của $it bài" }) { c, _ -> c.counter(Keys.HARD) },
        BadgeDef("perfect", "🎯", "Thần xạ thủ", "lượt", listOf(1, 5, 20), listOf(10, 30, 80),
            { "$it lượt làm đúng hết 12/12 câu" }) { c, _ -> c.counter(Keys.PERFECT) },
        BadgeDef("comeback", "🔁", "Kiên trì", "bài", listOf(1, 5, 15), listOf(10, 30, 80),
            { "Học lại xong $it bài bị trả về" }) { c, _ -> c.counter(Keys.RELEARN) },
        BadgeDef("exam", "🏆", "Nhà vô địch", "bài", listOf(1, 2, 4), listOf(30, 50, 150),
            { "Đạt $it bài kiểm tra học kì" }) { c, _ -> c.counter(Keys.EXAM) },
        BadgeDef("fighter", "🏅", "Đấu sĩ", "trận", listOf(1, 10, 30), listOf(10, 30, 80),
            { "Về nhất $it trận giải đấu" }) { c, _ -> c.counter(Keys.LEAGUE_WINS) },
        BadgeDef("collector", "🎨", "Nhà sưu tầm", "món", listOf(2, 6, 12), listOf(10, 20, 50),
            { "Sở hữu $it món trang trí" }) { c, _ -> c.owned.size }
    )

    val totalTiers get() = all.sumOf { it.targets.size }

    /** Các cấp đã đạt nhưng con chưa bấm nhận xu. */
    fun claimable(child: ChildProfile, data: FamilyData): List<Pair<BadgeDef, Int>> =
        all.flatMap { b ->
            val v = b.metric(child, data)
            b.targets.indices.filter { t -> v >= b.targets[t] && b.key(t) !in child.claimed }.map { b to it }
        }

    fun earnedCount(child: ChildProfile, data: FamilyData): Int = all.sumOf { it.reached(it.metric(child, data)) }
}
