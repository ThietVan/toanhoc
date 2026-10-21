package com.example.toanhoc.data.repository

import com.example.toanhoc.data.dao.SessionLogDao
import com.example.toanhoc.data.dao.UserKnowledgeDao
import com.example.toanhoc.data.entity.SessionLogEntity
import com.example.toanhoc.data.entity.UserKnowledgeEntity
import com.example.toanhoc.domain.model.KnowledgeComponent

class MathRepositoryImpl(
    private val userKnowledgeDao: UserKnowledgeDao,
    private val sessionLogDao: SessionLogDao
) : MathRepository {

    override suspend fun getKnowledgeComponentsForGrade(grade: Int): List<KnowledgeComponent> {
        return when (grade) {
            1 -> listOf(
                KnowledgeComponent("kc_g1_add_20", 1, "Cộng trừ trong phạm vi 20", "Cộng trong phạm vi 20", "Phản xạ cộng nhẩm 0-20"),
                KnowledgeComponent("kc_g1_sub_20", 1, "Cộng trừ trong phạm vi 20", "Trừ trong phạm vi 20", "Phản xạ trừ nhẩm 0-20")
            )
            2 -> listOf(
                KnowledgeComponent("kc_g2_add_100", 2, "Cộng trừ có nhớ", "Cộng có nhớ phạm vi 100", "Đặt tính cộng có nhớ"),
                KnowledgeComponent("kc_g2_sub_100", 2, "Cộng trừ có nhớ", "Trừ có nhớ phạm vi 100", "Đặt tính trừ có nhớ")
            )
            3 -> listOf(
                KnowledgeComponent("kc_g3_mul", 3, "Phép nhân bảng 6-9", "Bảng nhân 6-9", "Tính nhẩm nhân"),
                KnowledgeComponent("kc_g3_div", 3, "Phép chia bảng 6-9", "Bảng chia 6-9", "Tính nhẩm chia")
            )
            4 -> listOf(
                KnowledgeComponent("kc_g4_bar_model", 4, "Bài toán tìm 2 số", "Singapore Bar Model", "Giải bài toán Tổng - Hiệu"),
                KnowledgeComponent("kc_g4_fraction", 4, "Phân số", "Cộng trừ phân số", "Cộng trừ hai phân số khác mẫu")
            )
            5 -> listOf(
                KnowledgeComponent("kc_g5_speed_distance", 5, "Toán chuyển động", "S = v x t", "Tính quãng đường, vận tốc, thời gian"),
                KnowledgeComponent("kc_g5_decimal", 5, "Số thập phân", "Phép tính số thập phân", "Nhân chia số thập phân")
            )
            else -> emptyList()
        }
    }

    override suspend fun getKcMasteryMap(grade: Int): Map<String, Double> {
        val entities = userKnowledgeDao.getKnowledgeForGrade(grade)
        return entities.associate { it.kcId to it.masteryProbability }
    }

    override suspend fun saveKcMastery(kcId: String, grade: Int, mastery: Double) {
        userKnowledgeDao.saveKnowledge(
            UserKnowledgeEntity(
                kcId = kcId,
                grade = grade,
                masteryProbability = mastery,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun logAttempt(grade: Int, kcId: String, isCorrect: Boolean, responseLatencyMs: Long) {
        sessionLogDao.insertLog(
            SessionLogEntity(
                timestamp = System.currentTimeMillis(),
                grade = grade,
                kcId = kcId,
                isCorrect = isCorrect,
                responseLatencyMs = responseLatencyMs
            )
        )
    }
}
