package com.kotme

import com.kotme.model.AchievementDTO
import kotlinx.serialization.Serializable

@Serializable
data class ResultMessage(
    val status: ResultStatus,
    val message: String,
    val consoleLog: String,
    val newAchievements: List<AchievementDTO>
)