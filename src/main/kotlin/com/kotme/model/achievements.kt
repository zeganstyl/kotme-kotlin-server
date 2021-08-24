package com.kotme.model

import com.kotme.common.AchievementDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.select

object Achievements: IntIdTable() {
    val name = text("name")
    val conditionText = text("conditionText")
    val lastModifiedTime = long("lastModifiedTime").apply { defaultValueFun = { System.currentTimeMillis() } }
}

class Achievement(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Achievement>(Achievements) {
        fun new(name: String, conditionText: String) {
            if (Achievements.select { Achievements.name eq name }.count() == 0L) {
                Achievement.new {
                    this.name = name
                    this.conditionText = conditionText
                }
            }
        }
    }
    var name by Achievements.name
    var conditionText by Achievements.conditionText
    var lastModifiedTime by Achievements.lastModifiedTime

    fun dto(): AchievementDTO = AchievementDTO(id.value, name, conditionText)
}