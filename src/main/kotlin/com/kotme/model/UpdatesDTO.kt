package com.kotme.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatesDTO(
    val user: UserDTO,
    val exercises: List<ExerciseDTO>,
    val achievements: List<AchievementDTO>,
    val lastUpdateTime: Long = System.currentTimeMillis()
)