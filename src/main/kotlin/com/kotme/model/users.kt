package com.kotme.model

import io.ktor.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users: IntIdTable() {
    val name = varchar("name", 100)
    val login = varchar("login", 100)
    val password = text("password")
    val progress = integer("progress").default(0)
}

class User(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<User>(Users)
    var name by Users.name
    var login by Users.login
    var password by Users.password
    var progress by Users.progress

    val codes by UserCode referrersOn UserCodes.user
    val achievements by UserAchievement referrersOn UserAchievements.user
}

@Serializable
data class UserDTO(
    val id: Int,
    val name: String,
    val progress: Int,
    val codes: List<UserCodeDTO>,
    val achievements: List<UserAchievementDTO>
) {
    constructor(user: User): this(
        user.id.value,
        user.name,
        user.progress,
        user.codes.map { UserCodeDTO(it) },
        user.achievements.map { UserAchievementDTO(it) }
    )

    constructor(user: User, from: Long): this(
        user.id.value,
        user.name,
        user.progress,
        user.codes.mapNotNull { if (it.lastModifiedTime > from) UserCodeDTO(it) else null },
        user.achievements.mapNotNull { if (it.receiveTime > from) UserAchievementDTO(it) else null }
    )
}