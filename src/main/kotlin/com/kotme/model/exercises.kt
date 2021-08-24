package com.kotme.model

import com.kotme.Main
import com.kotme.common.ExerciseDTO
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

    fun dto(): ExerciseDTO = ExerciseDTO(
        id.value,
        number,
        name,
        readFileOrEmpty("/lessons/${number}lesson.md"),
        readFileOrEmpty("/lessons/${number}story.txt"),
        readFileOrEmpty("/lessons/${number}exercise.txt"),
        readFileOrEmpty("/lessons/${number}code.kt"),
        readFileOrEmpty("/lessons/${number}talk.txt")
    )

    private fun readFileOrEmpty(file: String) = Main::class.java.getResourceAsStream(file)?.readAllBytes().run {
        if (this != null) String(this) else ""
    }
}