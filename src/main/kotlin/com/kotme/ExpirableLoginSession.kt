package com.kotme

const val SESSION_LIVING_TIME = 900_000 // 15 minutes

data class ExpirableLoginSession(
    val userId: Int,
    val expiration: Long = System.currentTimeMillis() + SESSION_LIVING_TIME
)