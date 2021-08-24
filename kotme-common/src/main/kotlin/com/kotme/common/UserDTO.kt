package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val name: String,
    val progress: Int,
    val codes: List<UserCodeDTO>,
    val achievements: List<UserAchievementDTO>
)