package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class AchievementDTO(
    val id: Int,
    val name: String,
    val conditionText: String
)