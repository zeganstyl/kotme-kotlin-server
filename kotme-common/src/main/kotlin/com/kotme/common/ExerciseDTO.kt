package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDTO(
    val id: Int,
    val number: Int,
    val name: String,
    val lessonText: String,
    val storyText: String,
    val exerciseText: String,
    val initialCode: String,
    val characterMessage: String
)