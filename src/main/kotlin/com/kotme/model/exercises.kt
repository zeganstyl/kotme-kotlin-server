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
}

@Serializable
data class ExerciseDTO(
    var number: Int,
    var name: String,
    var lessonText: String,
    var exerciseText: String,
    var initialCode: String
) {
    constructor(e: Exercise): this(
        e.number,
        e.name,
        String(Main::class.java.getResourceAsStream("/static/lessons/${e.number}lesson.md")!!.readAllBytes()),
        String(Main::class.java.getResourceAsStream("/static/lessons/${e.number}exercise.txt")!!.readAllBytes()),
        String(Main::class.java.getResourceAsStream("/static/lessons/${e.number}code.kt")!!.readAllBytes())
    )
}