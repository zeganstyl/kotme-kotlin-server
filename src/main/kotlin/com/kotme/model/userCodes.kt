package com.kotme.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserCodes: IntIdTable() {
    val user = reference("user", Users)
    val exercise = reference("exercise", Exercises)
    val code = text("code")
    val uploadTime = long("uploadTime").apply { defaultValueFun = { System.currentTimeMillis() } }
    val completeTime = long("completeTime").default(0)
}

class UserCode(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserCode>(UserCodes)
    var user by User referencedOn UserCodes.user
    var exercise by Exercise referencedOn UserCodes.exercise
    var code by UserCodes.code
    var uploadTime by UserCodes.uploadTime
    var completeTime by UserCodes.completeTime
}

@Serializable
data class UserCodeDTO(
    var user: Int,
    var exercise: Int,
    var code: String,
    var uploadTime: Long,
    var completeTime: Long
) {
    constructor(code: UserCode): this(
        code.user.id.value,
        code.exercise.id.value,
        code.code,
        code.uploadTime,
        code.completeTime
    )
}