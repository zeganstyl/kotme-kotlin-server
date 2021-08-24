package com.kotme.model

import kotlinx.serialization.Serializable

@Serializable
data class CodeCheckResult(
    val status: CodeCheckResultStatus,
    val message: String,
    val errors: String,
    val consoleLog: String,
    val newAchievements: List<UserAchievementDTO>
)