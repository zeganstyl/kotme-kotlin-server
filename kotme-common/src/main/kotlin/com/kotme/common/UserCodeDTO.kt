package com.kotme.common

import kotlinx.serialization.Serializable

@Serializable
data class UserCodeDTO(
    var user: Int,
    var exercise: Int,
    var code: String,
    var lastModifiedTime: Long,
    var completeTime: Long,
    var resultStatus: CodeCheckResultStatus,
    var resultErrors: String,
    var consoleLog: String
)