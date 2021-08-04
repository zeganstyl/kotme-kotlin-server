package com.kotme.model

import io.ktor.auth.*
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