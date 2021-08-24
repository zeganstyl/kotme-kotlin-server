package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class UserAchievementDTO(
    val user: Int,
    val achievement: Int,
    val receiveTime: Long
)