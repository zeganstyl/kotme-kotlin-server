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
    val lastModifiedTime = long("lastModifiedTime").apply { defaultValueFun = { System.currentTimeMillis() } }
    val completeTime = long("completeTime").default(0)
    val resultStatus = enumeration("resultStatus", CodeCheckResultStatus::class).default(CodeCheckResultStatus.NoStatus)
    val resultMessage = text("resultMessage").default("")
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
    var resultMessage by UserCodes.resultMessage
    var resultErrors by UserCodes.resultErrors
    var consoleLog by UserCodes.consoleLog
}

@Serializable
data class UserCodeDTO(
    var user: Int,
    var exercise: Int,
    var code: String,
    var lastModifiedTime: Long,
    var completeTime: Long,
    var resultStatus: CodeCheckResultStatus,
    var resultMessage: String,
    var resultErrors: String,
    var consoleLog: String
) {
    constructor(code: UserCode): this(
        code.user.id.value,
        code.exercise.id.value,
        code.code,
        code.lastModifiedTime,
        code.completeTime,
        code.resultStatus,
        code.resultMessage,
        code.resultErrors,
        code.consoleLog
    )
}