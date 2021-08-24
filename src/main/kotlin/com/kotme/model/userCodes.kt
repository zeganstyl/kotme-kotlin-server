package com.kotme.model

import com.kotme.common.CodeCheckResultStatus
import com.kotme.common.UserCodeDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserCodes: IntIdTable() {
    val user = reference("user", Users)
    val exercise = reference("exercise", Exercises)
    val code = text("code")
    val lastModifiedTime = long("lastModifiedTime").apply { defaultValueFun = { System.currentTimeMillis() } }
    val completeTime = long("completeTime").default(0)
    val resultStatus = enumeration("resultStatus", CodeCheckResultStatus::class).default(CodeCheckResultStatus.NoStatus)
    val resultErrors = text("resultErrors").default("")
    val consoleLog = text("consoleLog").default("")
}

class UserCode(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserCode>(UserCodes)
    var user by User referencedOn UserCodes.user
    var exercise by Exercise referencedOn UserCodes.exercise
    var code by UserCodes.code
    var lastModifiedTime by UserCodes.lastModifiedTime
    var completeTime by UserCodes.completeTime
    var resultStatus by UserCodes.resultStatus
    var resultErrors by UserCodes.resultErrors
    var consoleLog by UserCodes.consoleLog

    fun dto(): UserCodeDTO = UserCodeDTO(
        user.id.value,
        exercise.id.value,
        code,
        lastModifiedTime,
        completeTime,
        resultStatus,
        resultErrors,
        consoleLog
    )
}