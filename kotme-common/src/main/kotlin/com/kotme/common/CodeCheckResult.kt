package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class CodeCheckResult(
    val status: CodeCheckResultStatus,
    val errors: String,
    val consoleLog: String,
    val newAchievements: List<UserAchievementDTO>
)