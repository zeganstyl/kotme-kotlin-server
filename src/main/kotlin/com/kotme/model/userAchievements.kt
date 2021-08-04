package com.kotme.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserAchievements: IntIdTable() {
    val user = reference("user", Users)
    val achievement = reference("achievement", Achievements)
    val receiveTime = long("receiveTime").apply { defaultValueFun = { System.currentTimeMillis() } }
}

class UserAchievement(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserAchievement>(UserAchievements) {
        fun new(user: User, achievement: Achievement) = UserAchievement.new {
            this.user = user
            this.achievement = achievement
        }
    }
    var user by User referencedOn UserAchievements.user
    var achievement by Achievement referencedOn UserAchievements.achievement
    val receiveTime by UserAchievements.receiveTime
}

@Serializable
data class UserAchievementDTO(
    var user: Int,
    var achievement: Int,
    val receiveTime: Long
) {
    constructor(a: UserAchievement): this(a.user.id.value, a.achievement.id.value, a.receiveTime)
}