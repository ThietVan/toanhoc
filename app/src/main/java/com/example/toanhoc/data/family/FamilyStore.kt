package com.example.toanhoc.data.family

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime

/** Kết quả nhập mã PIN phụ huynh. */
sealed interface PinResult {
    data object Ok : PinResult
    data class Wrong(val attemptsLeft: Int) : PinResult
    data class Locked(val secondsLeft: Int) : PinResult
}

/**
 * Kho dữ liệu gia đình (SharedPreferences + JSON), là nguồn sự thật cho:
 * hồ sơ các con, hồ sơ đang học, mã PIN phụ huynh, xu, quà, nhật ký học và câu sai.
 *
 * Mã PIN và năm sinh khôi phục chỉ lưu dạng băm SHA-256 có muối (không lưu số thật).
 * Nhập sai 5 lần → khóa 60 giây.
 */
class FamilyStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("family", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Mỗi lần con được cộng xu (để hiện thông báo "+10 xu"). */
    private val _awards = MutableSharedFlow<CoinEvent>(extraBufferCapacity = 16)
    val awards: SharedFlow<CoinEvent> = _awards.asSharedFlow()

    private val _data = MutableStateFlow(load())
    val data: StateFlow<FamilyData> = _data.asStateFlow()

    private val _activeChildId = MutableStateFlow(prefs.getString(KEY_ACTIVE, null)?.takeIf { id -> _data.value.child(id) != null })
    val activeChildId: StateFlow<String?> = _activeChildId.asStateFlow()

    private fun load(): FamilyData {
        val raw = prefs.getString(KEY_DATA, null)?.let { runCatching { json.decodeFromString(FamilyData.serializer(), it) }.getOrNull() } ?: FamilyData()
        val migrated = migrateArt(raw)
        if (migrated != raw) prefs.edit().putString(KEY_DATA, json.encodeToString(FamilyData.serializer(), migrated)).apply()
        return migrated
    }

    /** Chuyển hồ sơ từ avatar con vật cũ sang nhân vật mới: hoàn lại xu đã mua đồ cũ. */
    private fun migrateArt(d: FamilyData): FamilyData {
        if (d.children.all { it.artVersion >= 2 }) return d
        val now = System.currentTimeMillis()
        val events = ArrayList<CoinEvent>()
        val children = d.children.map { c ->
            if (c.artVersion >= 2) c else {
                val legacy = c.owned.filter { k -> listOf("ANIMAL:", "BACKGROUND:", "FRAME:", "ACCESSORY:").any { k.startsWith(it) } }
                val refund = legacy.sumOf { AvatarShop.LEGACY_PRICES[it] ?: 0 }
                if (refund > 0) events += CoinEvent(c.id, now, refund, "Hoàn xu đồ trang trí cũ")
                c.copy(owned = c.owned - legacy.toSet(), coins = c.coins + refund, artVersion = 2, hair = AvatarShop.defaultHair(c.gender))
            }
        }
        return d.copy(children = children, coinLog = (d.coinLog + events).takeLast(MAX_COIN_LOG))
    }

    @Synchronized
    private fun update(block: (FamilyData) -> FamilyData): FamilyData {
        val next = block(_data.value)
        prefs.edit().putString(KEY_DATA, json.encodeToString(FamilyData.serializer(), next)).apply()
        _data.value = next
        return next
    }

    private fun today() = LocalDate.now().toString()

    /* ------------------------------------ Mã PIN ------------------------------------ */

    fun hasPin(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    private fun hash(value: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest("$salt:$value".toByteArray()).joinToString("") { "%02x".format(it) }

    private fun newSalt(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

    /** Tạo mã PIN lần đầu, kèm năm sinh phụ huynh (4 số) để lấy lại mã khi quên. */
    fun setPin(pin: String, recoveryYear: String) {
        val salt = newSalt()
        val recoverySalt = newSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash(pin, salt))
            .putString(KEY_RECOVERY_SALT, recoverySalt)
            .putString(KEY_RECOVERY_HASH, hash(recoveryYear, recoverySalt))
            .putInt(KEY_FAILS, 0)
            .putLong(KEY_LOCKED_UNTIL, 0L)
            .apply()
    }

    fun hasRecovery(): Boolean = prefs.getString(KEY_RECOVERY_HASH, null) != null

    fun verifyPin(pin: String): PinResult {
        val now = System.currentTimeMillis()
        val lockedUntil = prefs.getLong(KEY_LOCKED_UNTIL, 0L)
        if (lockedUntil > now) return PinResult.Locked(((lockedUntil - now) / 1000L).toInt() + 1)
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return PinResult.Wrong(MAX_FAILS)
        if (hash(pin, salt) == prefs.getString(KEY_PIN_HASH, null)) {
            prefs.edit().putInt(KEY_FAILS, 0).apply()
            return PinResult.Ok
        }
        val fails = prefs.getInt(KEY_FAILS, 0) + 1
        return if (fails >= MAX_FAILS) {
            prefs.edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCKED_UNTIL, now + LOCK_MS).apply()
            PinResult.Locked((LOCK_MS / 1000L).toInt())
        } else {
            prefs.edit().putInt(KEY_FAILS, fails).apply()
            PinResult.Wrong(MAX_FAILS - fails)
        }
    }

    fun verifyRecovery(year: String): Boolean {
        val salt = prefs.getString(KEY_RECOVERY_SALT, null) ?: return false
        return hash(year, salt) == prefs.getString(KEY_RECOVERY_HASH, null)
    }

    /** Đổi PIN, giữ nguyên năm sinh khôi phục. */
    fun changePin(pin: String) {
        val salt = newSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash(pin, salt))
            .putInt(KEY_FAILS, 0)
            .putLong(KEY_LOCKED_UNTIL, 0L)
            .apply()
    }

    /* --------------------------------- Hồ sơ các con --------------------------------- */

    fun addChild(name: String, birthYear: Int, grade: Int, gender: String, skin: String): ChildProfile {
        var created: ChildProfile? = null
        update { d ->
            val child = ChildProfile(
                id = "c${d.nextId}", name = name.trim(), birthYear = birthYear, grade = grade,
                gender = gender, skin = skin, hair = AvatarShop.defaultHair(gender), artVersion = 2
            )
            created = child
            d.copy(children = d.children + child, nextId = d.nextId + 1)
        }
        return created!!
    }

    fun updateChild(child: ChildProfile) {
        update { d -> d.copy(children = d.children.map { if (it.id == child.id) child else it }) }
    }

    private fun updateChild(id: String, block: (ChildProfile) -> ChildProfile) {
        update { d -> d.copy(children = d.children.map { if (it.id == id) block(it) else it }) }
    }

    /** Xóa hồ sơ con và toàn bộ nhật ký của con trên máy (không hoàn tác). */
    fun deleteChild(id: String) {
        update { d ->
            d.copy(
                children = d.children.filterNot { it.id == id },
                rewards = d.rewards.filterNot { it.childId == id },
                requests = d.requests.filterNot { it.childId == id },
                coinLog = d.coinLog.filterNot { it.childId == id },
                mistakes = d.mistakes.filterNot { it.childId == id },
                studySeconds = d.studySeconds - id,
                dailyBonusDays = d.dailyBonusDays - id,
                challenges = d.challenges.filterNot { it.childId == id }
            )
        }
        if (_activeChildId.value == id) setActive(null)
    }

    fun setActive(id: String?) {
        _activeChildId.value = id
        prefs.edit().putString(KEY_ACTIVE, id).apply()
    }

    /* -------------------------------------- Xu -------------------------------------- */

    fun award(childId: String, amount: Int, reason: String, notify: Boolean = true) {
        if (amount == 0) return
        val event = CoinEvent(childId, System.currentTimeMillis(), amount, reason)
        update { d ->
            d.copy(
                children = d.children.map { if (it.id == childId) it.copy(coins = (it.coins + amount).coerceAtLeast(0)) else it },
                coinLog = (d.coinLog + event).takeLast(MAX_COIN_LOG)
            )
        }
        if (notify && amount > 0) _awards.tryEmit(event)
    }

    /* ----------------------------------- Huy hiệu ----------------------------------- */

    fun bump(childId: String, key: String, by: Int = 1) =
        updateChild(childId) { c -> c.copy(counters = c.counters + (key to c.counter(key) + by)) }

    /** Đặt bộ đếm tối thiểu bằng [value] (bù thành tích đã học trước khi có huy hiệu). */
    fun ensureAtLeast(childId: String, key: String, value: Int) =
        updateChild(childId) { c -> if (c.counter(key) >= value) c else c.copy(counters = c.counters + (key to value)) }

    /** Con bấm "Nhận" ở một cấp huy hiệu đã đạt → cộng xu, đánh dấu đã nhận. */
    fun claimBadge(childId: String, badge: BadgeDef, tier: Int): Boolean {
        val d = _data.value
        val child = d.child(childId) ?: return false
        if (badge.key(tier) in child.claimed || badge.metric(child, d) < badge.targets[tier]) return false
        updateChild(childId) { it.copy(claimed = it.claimed + badge.key(tier)) }
        award(childId, badge.coins[tier], "Huy hiệu ${badge.name} · ${BadgeTier.entries[tier].label}")
        return true
    }

    /** Mua đồ trang trí: trừ xu, thêm vào tủ đồ và dùng luôn. */
    fun buy(childId: String, item: ShopItem): Boolean {
        val child = _data.value.child(childId) ?: return false
        if (AvatarShop.isOwned(child, item)) { equip(childId, item); return true }
        if (child.coins < item.price) return false
        update { d ->
            d.copy(
                children = d.children.map { if (it.id == childId) it.copy(coins = it.coins - item.price, owned = it.owned + AvatarShop.key(item)) else it },
                coinLog = (d.coinLog + CoinEvent(childId, System.currentTimeMillis(), -item.price, "Mua ${item.name}")).takeLast(MAX_COIN_LOG)
            )
        }
        equip(childId, item)
        return true
    }

    fun equip(childId: String, item: ShopItem) {
        updateChild(childId) { c ->
            if (!AvatarShop.isOwned(c, item)) c else when (item.kind) {
                ShopKind.SKIN -> c.copy(skin = item.id)
                ShopKind.HAIR -> c.copy(hair = item.id)
                ShopKind.HAIR_COLOR -> c.copy(hairColor = item.id)
                ShopKind.OUTFIT -> c.copy(outfit = item.id)
                ShopKind.SCENE -> c.copy(scene = item.id)
                ShopKind.PET -> c.copy(pet = item.id)
                ShopKind.ACCESSORY -> c.copy(accessory = item.id)
            }
        }
    }

    /** Đổi bé trai / bé gái (miễn phí). Nếu đang để tóc mặc định thì đổi sang tóc mặc định của giới kia. */
    fun setGender(childId: String, gender: String) = updateChild(childId) { c ->
        val hair = if (c.hair == AvatarShop.defaultHair(c.gender)) AvatarShop.defaultHair(gender) else c.hair
        c.copy(gender = gender, hair = hair)
    }

    /* ------------------------------------- Quà ------------------------------------- */

    fun addReward(title: String, emoji: String, cost: Int, childId: String? = null) {
        update { d -> d.copy(rewards = d.rewards + Reward("r${d.nextId}", title.trim(), emoji, cost, childId), nextId = d.nextId + 1) }
    }

    fun deleteReward(id: String) {
        update { d -> d.copy(rewards = d.rewards.filterNot { it.id == id }) }
    }

    /** Con xin đổi quà: trừ xu ngay, chờ bố mẹ trao quà thật rồi bấm "Đã trao". */
    fun requestReward(childId: String, reward: Reward): Boolean {
        val child = _data.value.child(childId) ?: return false
        if (child.coins < reward.cost) return false
        update { d ->
            val now = System.currentTimeMillis()
            d.copy(
                children = d.children.map { if (it.id == childId) it.copy(coins = it.coins - reward.cost) else it },
                requests = d.requests + RewardRequest("q${d.nextId}", childId, reward.id, reward.title, reward.emoji, reward.cost, now),
                coinLog = (d.coinLog + CoinEvent(childId, now, -reward.cost, "Đổi quà: ${reward.title}")).takeLast(MAX_COIN_LOG),
                nextId = d.nextId + 1
            )
        }
        return true
    }

    fun markDelivered(requestId: String) {
        update { d -> d.copy(requests = d.requests.map { if (it.id == requestId) it.copy(deliveredAt = System.currentTimeMillis()) else it }) }
    }

    /** Bố mẹ từ chối / hủy yêu cầu: hoàn lại xu cho con. */
    fun cancelRequest(requestId: String) {
        val req = _data.value.requests.firstOrNull { it.id == requestId && !it.delivered } ?: return
        update { d -> d.copy(requests = d.requests.filterNot { it.id == requestId }) }
        award(req.childId, req.cost, "Hoàn xu: ${req.title}", notify = false)
    }

    /* ---------------------------------- Nhật ký học ---------------------------------- */

    /** Cộng thời gian học; lần đầu trong ngày học đủ 1 phút → thưởng "Mỗi ngày có học" (+ chuỗi 7 ngày). */
    fun addStudySeconds(childId: String, seconds: Int) {
        val day = today()
        val after = update { d ->
            val perChild = d.studySeconds[childId].orEmpty()
            d.copy(studySeconds = d.studySeconds + (childId to (perChild + (day to (perChild[day] ?: 0) + seconds))))
        }
        val reachedMinute = (after.studySeconds[childId]?.get(day) ?: 0) >= 60
        val alreadyGiven = after.dailyBonusDays[childId]?.contains(day) == true
        if (reachedMinute && !alreadyGiven) {
            update { d -> d.copy(dailyBonusDays = d.dailyBonusDays + (childId to (d.dailyBonusDays[childId].orEmpty() + day).takeLastSet(60))) }
            award(childId, CoinRules.DAILY, "Học hôm nay")
            val streak = _data.value.streak(childId)
            updateChild(childId) { c -> if (streak > c.counter(Badges.Keys.BEST_STREAK)) c.copy(counters = c.counters + (Badges.Keys.BEST_STREAK to streak)) else c }
        }
    }

    /* ------------------------------ Lộ trình & thử thách vượt bài ------------------------------ */

    /** Mã cho thử thách mới (dùng chung bộ đếm id). */
    fun newChallengeId(): String {
        var id = ""
        update { d -> id = "x${d.nextId}"; d.copy(nextId = d.nextId + 1) }
        return id
    }

    /** Lưu thử thách mới; thử thách cũ chưa làm xong của con bị thay thế. Giữ tối đa 12 lần gần nhất mỗi con. */
    fun saveChallenge(ch: com.example.toanhoc.domain.challenge.Challenge) {
        update { d ->
            val others = d.challenges.filterNot { it.childId == ch.childId && !it.finished }
            val mine = (others.filter { it.childId == ch.childId } + ch).takeLast(12)
            d.copy(challenges = others.filterNot { it.childId == ch.childId } + mine)
        }
    }

    fun answerChallenge(id: String, unit: Int, question: Int, picked: String, ms: Long) {
        update { d -> d.copy(challenges = d.challenges.map { if (it.id == id) it.answer(unit, question, picked, ms) else it }) }
    }

    fun finishChallenge(id: String) {
        update { d -> d.copy(challenges = d.challenges.map { if (it.id == id && it.finishedAt == null) it.copy(finishedAt = System.currentTimeMillis()) else it }) }
    }

    fun markChallengeSeen(id: String) {
        update { d -> d.copy(challenges = d.challenges.map { if (it.id == id) it.copy(seenByParent = true) else it }) }
    }

    fun cancelChallenge(id: String) {
        update { d -> d.copy(challenges = d.challenges.filterNot { it.id == id && !it.finished }) }
    }

    fun recordMistake(entry: MistakeEntry) {
        update { d -> d.copy(mistakes = (d.mistakes + entry).takeLast(MAX_MISTAKES)) }
    }

    /* ----------------------------------- Giải đấu ----------------------------------- */

    /** Trạng thái giải của con (tự sang tuần/ngày mới và lưu lại nếu có thay đổi). */
    fun league(childId: String, now: LocalDateTime = LocalDateTime.now()): LeagueState {
        val child = _data.value.child(childId) ?: return LeagueState()
        val rolled = League.roll(child.league, childId, now)
        if (rolled != child.league) updateChild(childId) { it.copy(league = rolled) }
        return rolled
    }

    /** Vào trận: dùng vé miễn phí trong ngày hoặc trả phí. Trả về null nếu không vào được. */
    fun startMatch(childId: String): EntryKind? {
        val st = league(childId)
        val child = _data.value.child(childId) ?: return null
        return when (val kind = League.entry(st, child.coins)) {
            EntryKind.FREE -> {
                updateChild(childId) { it.copy(league = st.copy(freeUsedToday = true, matchesToday = st.matchesToday + 1)) }
                kind
            }
            EntryKind.PAID -> {
                updateChild(childId) { it.copy(league = st.copy(matchesToday = st.matchesToday + 1)) }
                award(childId, -League.FEE, "Phí vào giải đấu", notify = false)
                kind
            }
            else -> null
        }
    }

    /** Kết thúc trận: cộng điểm giải, thưởng xu theo hạng, chỉnh mức bạn thi. [forfeit] = bỏ cuộc giữa chừng. */
    fun finishMatch(childId: String, place: Int, correct: Int, answered: Int, points: Int, forfeit: Boolean) {
        updateChild(childId) { c ->
            val l = c.league
            c.copy(
                league = l.copy(
                    points = l.points + if (forfeit) 0 else points,
                    matchesThisWeek = l.matchesThisWeek + 1,
                    rating = if (forfeit) l.rating else League.adapt(l.rating, place),
                    played = l.played + 1,
                    wins = l.wins + if (!forfeit && place == 1) 1 else 0,
                    correct = l.correct + correct,
                    answered = l.answered + answered
                ),
                counters = if (!forfeit && place == 1) c.counters + (Badges.Keys.LEAGUE_WINS to c.counter(Badges.Keys.LEAGUE_WINS) + 1) else c.counters
            )
        }
        if (!forfeit) award(childId, League.PAYOUT[(place - 1).coerceIn(0, 4)], "Giải đấu · về hạng $place")
    }

    fun ackLeagueWeek(childId: String) =
        updateChild(childId) { c -> c.copy(league = c.league.copy(lastWeek = c.league.lastWeek?.copy(seen = true))) }

    /* -------------------------------- Giới hạn thời gian -------------------------------- */

    fun setDailyLimit(childId: String, minutes: Int) = updateChild(childId) { it.copy(dailyLimitMinutes = minutes) }

    fun grantBonusMinutes(childId: String, minutes: Int) {
        val day = today()
        updateChild(childId) { c -> c.copy(bonusMinutes = mapOf(day to (c.bonusMinutes[day] ?: 0) + minutes)) }
    }

    /** Số phút còn được học hôm nay (null = không giới hạn). */
    fun minutesLeftToday(child: ChildProfile, data: FamilyData = _data.value): Int? {
        if (child.dailyLimitMinutes <= 0) return null
        val allowed = child.dailyLimitMinutes + (child.bonusMinutes[today()] ?: 0)
        return (allowed - data.todayMinutes(child.id)).coerceAtLeast(0)
    }

    companion object {
        private const val KEY_DATA = "data"
        private const val KEY_ACTIVE = "active_child"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_RECOVERY_HASH = "recovery_hash"
        private const val KEY_RECOVERY_SALT = "recovery_salt"
        private const val KEY_FAILS = "pin_fails"
        private const val KEY_LOCKED_UNTIL = "pin_locked_until"
        const val MAX_FAILS = 5
        private const val LOCK_MS = 60_000L
        private const val MAX_COIN_LOG = 300
        private const val MAX_MISTAKES = 400
    }
}

private fun Set<String>.takeLastSet(n: Int): Set<String> = if (size <= n) this else sorted().takeLast(n).toSet()
