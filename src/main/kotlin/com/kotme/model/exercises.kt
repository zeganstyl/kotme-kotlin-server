package com.kotme.model

import com.kotme.Main
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.select

object Exercises: IntIdTable() {
    val number = integer("number")
    val name = text("name")
    val lastModifiedTime = long("lastModifiedTime").apply { defaultValueFun = { System.currentTimeMillis() } }
}

class Exercise(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Exercise>(Exercises) {
        fun new(number: Int, name: String) {
            if (Exercises.select { Exercises.number eq number }.count() == 0L) {
                Exercise.new {
                    this.number = number
                    this.name = name
                }
            }
        }
    }
    var number by Exercises.number
    var name by Exercises.name
    var lastModifiedTime by Exercises.lastModifiedTime
}

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
) {
    constructor(e: Exercise): this(
        e.id.value,
        e.number,
        e.name,
        readFileOrEmpty("/lessons/${e.number}lesson.md"),
        readFileOrEmpty("/lessons/${e.number}story.txt"),
        readFileOrEmpty("/lessons/${e.number}exercise.txt"),
        readFileOrEmpty("/lessons/${e.number}code.kt"),
        readFileOrEmpty("/lessons/${e.number}message.txt")
    )
}

fun readFileOrEmpty(file: String) = Main::class.java.getResourceAsStream(file)?.readAllBytes().run {
    if (this != null) String(this) else ""
}