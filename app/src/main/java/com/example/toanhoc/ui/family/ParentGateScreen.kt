package com.example.toanhoc.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.toanhoc.data.family.FamilyStore
import com.example.toanhoc.data.family.PinResult
import kotlinx.coroutines.delay
import java.time.LocalDate

/** Cổng phụ huynh: tạo mã lần đầu, nhập mã, quên mã (khôi phục bằng năm sinh), đổi mã. */
enum class GateMode { CREATE, VERIFY, CHANGE }

private enum class Step { NEW_PIN, CONFIRM_PIN, RECOVERY_YEAR, VERIFY, RECOVER }

@Composable
fun ParentGateScreen(
    store: FamilyStore,
    mode: GateMode,
    purpose: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCancel)
    var step by remember { mutableStateOf(if (mode == GateMode.VERIFY) Step.VERIFY else Step.NEW_PIN) }
    var firstPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var lockedSeconds by remember { mutableIntStateOf(0) }
    var resetKey by remember { mutableIntStateOf(0) }
    var recoverFails by remember { mutableIntStateOf(0) }
    // Đang khôi phục mã (đã qua câu hỏi năm sinh) → tạo mã mới nhưng không đổi năm khôi phục
    var recovering by remember { mutableStateOf(false) }

    LaunchedEffect(lockedSeconds) {
        if (lockedSeconds > 0) {
            delay(1000)
            lockedSeconds -= 1
            if (lockedSeconds == 0) error = null
        }
    }

    fun next(s: Step, err: String? = null) { step = s; error = err; resetKey++ }

    val currentYear = LocalDate.now().year
    val (title, subtitle) = when (step) {
        Step.NEW_PIN -> (if (mode == GateMode.CREATE) "Tạo mã PIN phụ huynh" else "Nhập mã PIN mới") to
            "Mã 4 số để vào khu phụ huynh. Đừng cho con biết mã này nhé."
        Step.CONFIRM_PIN -> "Nhập lại mã PIN" to "Để chắc chắn bạn nhớ đúng mã."
        Step.RECOVERY_YEAR -> "Năm sinh của bố/mẹ" to "Dùng để lấy lại mã khi quên. Ví dụ: 1990"
        Step.VERIFY -> "Khu phụ huynh" to purpose
        Step.RECOVER -> "Quên mã PIN?" to "Nhập năm sinh của bố/mẹ đã khai khi tạo mã."
    }

    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(Modifier.fillMaxSize()) {
            BackRow(title = "", onBack = onCancel)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).align(Alignment.CenterHorizontally).widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(FamilyTone.Parent), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(30.dp))
                }
                PinEntry(
                    title = title,
                    subtitle = subtitle,
                    error = if (lockedSeconds > 0) "Nhập sai nhiều lần. Thử lại sau $lockedSeconds giây." else error,
                    resetKey = resetKey,
                    enabled = lockedSeconds == 0,
                    secret = step != Step.RECOVERY_YEAR && step != Step.RECOVER,
                    leftAction = if (step == Step.VERIFY && store.hasRecovery()) "Quên mã?" to { next(Step.RECOVER) } else null,
                    onComplete = { code ->
                        when (step) {
                            Step.NEW_PIN -> {
                                if (code.toSet().size == 1 || code in listOf("1234", "4321", "0123")) {
                                    next(Step.NEW_PIN, "Mã quá dễ đoán, hãy chọn mã khác")
                                } else { firstPin = code; next(Step.CONFIRM_PIN) }
                            }
                            Step.CONFIRM_PIN -> {
                                if (code != firstPin) next(Step.NEW_PIN, "Hai lần nhập không khớp, tạo lại nhé")
                                else if (mode == GateMode.CREATE && !recovering) next(Step.RECOVERY_YEAR)
                                else { store.changePin(firstPin); onSuccess() }
                            }
                            Step.RECOVERY_YEAR -> {
                                val y = code.toInt()
                                if (y !in (currentYear - 90)..(currentYear - 15)) next(Step.RECOVERY_YEAR, "Năm sinh chưa hợp lệ")
                                else { store.setPin(firstPin, code); onSuccess() }
                            }
                            Step.VERIFY -> when (val r = store.verifyPin(code)) {
                                PinResult.Ok -> onSuccess()
                                is PinResult.Wrong -> next(Step.VERIFY, "Mã chưa đúng · còn ${r.attemptsLeft} lần thử")
                                is PinResult.Locked -> { lockedSeconds = r.secondsLeft; next(Step.VERIFY) }
                            }
                            Step.RECOVER -> {
                                if (store.verifyRecovery(code)) { recovering = true; next(Step.NEW_PIN) }
                                else {
                                    recoverFails++
                                    if (recoverFails >= 3) { recoverFails = 0; lockedSeconds = 60; next(Step.VERIFY) }
                                    else next(Step.RECOVER, "Chưa đúng · còn ${3 - recoverFails} lần thử")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
