package com.kotme.routes

import com.kotme.exercise.EvalResult
import com.kotme.Main
import com.kotme.*
import com.kotme.common.*
import com.kotme.exercise.*
import com.kotme.model.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics

fun ApplicationCall.int(name: String): Int = parameters.getOrFail<Int>(name)

class ForbiddenException: Exception("Forbidden")

class UnauthorizedException: Exception("Unauthorized")

fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

suspend fun authenticate(login: String, password: String): User? {
    if (login.isEmpty()) return null
    val u = newSuspendedTransaction { User.find { Users.login eq login }.firstOrNull() }
    return if (u != null && u.password.isNotEmpty() && BCrypt.checkpw(password, u.password)) u else null
}

suspend fun ApplicationCall.authorizeAPI(block: suspend (user: User) -> Unit) {
    newSuspendedTransaction { block(authentication.principal() ?: throw UnauthorizedException()) }
}

suspend fun ApplicationCall.authorizeAPIOrNull(block: suspend (user: User?) -> Unit) {
    newSuspendedTransaction { block(authentication.principal()) }
}

fun checkCode(user: User?, exercise: Exercise, code: String): CodeCheckResult {
    var errors = ""
    var status = CodeCheckResultStatus.Success
    var consoleLog: String

    val row = if (user != null) UserCodes.select {
        (UserCodes.user eq user.id.value) and (UserCodes.exercise eq exercise.id.value)
    }.firstOrNull() else null

    val userCode = if (row != null) {
        UserCode.wrapRow(row).apply {
            this.code = code
        }
    } else if (user != null) {
        UserCode.new {
            this.exercise = exercise
            this.user = user
            this.code = code
        }
    } else null

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)

    System.setOut(ps)

    try {
        val result: EvalResult? = when (exercise.number) {
            1 -> exe1(code, os)
            2 -> exe2(code)
            3 -> exe3(code, os)
            4 -> exe4(code)
            5 -> exe5(code)
            6 -> exe6(code)
            7 -> exe7(code)
            8 -> exe8(code)
            9 -> exe9(code)
            10 -> exe10(code)
            else -> throw NotFoundException()
        }

        System.setOut(Main.console)

        when (result) {
            is ResultWithDiagnostics.Failure -> {
                status = CodeCheckResultStatus.CompileErrors
                val str = StringBuilder()
                result.reports.forEach {
                    str.append(it.render())
                    str.append('\n')
                }
                errors = str.toString()
            }
            is ResultWithDiagnostics.Success -> {
                status = CodeCheckResultStatus.Success
                val returnValue = result.value.returnValue
                if (returnValue is ResultValue.Error) {
                    status = CodeCheckResultStatus.RuntimeErrors
                    returnValue.error.message?.also { errors = it }
                }
            }
        }

        consoleLog = os.toString()
    } catch (ex: Exception) {
        status = CodeCheckResultStatus.ServerError
        consoleLog = os.toString()
    }

    consoleLog = consoleLog.takeLast(65536)

    System.setOut(Main.console)

    val currentTime = System.currentTimeMillis()

    userCode?.also {
        it.resultStatus = status
        it.resultErrors = errors
        it.consoleLog = consoleLog
        it.lastModifiedTime = currentTime
    }

    // new achievements ====
    val newAchievements = ArrayList<UserAchievementDTO>()
    if (status == CodeCheckResultStatus.Success) {
        if (user != null) {
            if (userCode?.completeTime == 0L) {
                userCode.completeTime = currentTime
            }

            val achivs = user.achievements
            if (exercise.number == 1) {
                if (achivs.find { it.achievement.id.value == 1 } == null) {
                    newAchievements.add(UserAchievement.new(user, Achievement[1]).dto())
                }
            }
            if (achivs.find { it.achievement.id.value == 2 } == null) {
                if (user.codes.count { it.completeTime != 0L } >= 5) {
                    newAchievements.add(UserAchievement.new(user, Achievement[2]).dto())
                }
            }
            if (achivs.find { it.achievement.id.value == 3 } == null) {
                if (user.codes.count { it.completeTime != 0L } == 10) {
                    newAchievements.add(UserAchievement.new(user, Achievement[3]).dto())
                }
            }
        }
    }
    // ====

    return CodeCheckResult(status, errors, consoleLog, newAchievements)
}

fun Routing.apiRoutes() {
    route(PATH.api) {
        authenticate("basic") {
            route(PATH.token) {
                get {
                    call.authorizeAPI {
                        call.respond(HttpStatusCode.OK, JwtConfig.makeToken(it))
                    }
                }
            }
        }
        route(PATH.signup) {
            post {
                newSuspendedTransaction {
                    val params = call.receiveParameters()

                    val name = params.getOrFail<String>(ID.name)
                    if (name.isEmpty()) {
                        throw BadRequestException(Message.nameIsEmpty)
                    }

                    val login = params.getOrFail<String>(ID.login)
                    if (Users.select { Users.login eq login }.count() > 0) {
                        throw BadRequestException(Message.loginAlreadyExists)

                    }

                    val password = params.getOrFail<String>(ID.password)
                    if (password.isEmpty()) {
                        throw BadRequestException(Message.incorrectPassword)
                    }

                    User.new {
                        this.name = name
                        this.login = login
                        this.password = hashPassword(password)
                    }

                    call.respond("")
                }
            }
        }
        route(PATH.achievements) {
            get {
                newSuspendedTransaction {
                    call.respond(Achievement.all().map { it.dto() })
                }
            }
        }
        authenticate("jwt") {
            route("/updates/{${ID.from}}") {
                get {
                    call.authorizeAPI { user ->
                        val from = call.parameters.getOrFail<Long>(ID.from)
                        call.respond(
                            UpdatesDTO(
                                user.dto(from),
                                Exercise.find { Exercises.lastModifiedTime greater from }.map { it.dto() },
                                Achievement.find { Achievements.lastModifiedTime greater from }.map { it.dto() }
                            )
                        )
                    }
                }
            }
            route(PATH.user) {
                route(PATH.codes) {
                    get {
                        call.authorizeAPI { user ->
                            call.respond(user.codes.map { it.dto() })
                        }
                    }
                    route("/{${ID.exercise}}") {
                        get {
                            call.authorizeAPI { user ->
                                val exe = call.int(ID.exercise)
                                call.respond(
                                    UserCode.wrapRow(
                                        UserCodes.select { (UserCodes.user eq user.id) and (UserCodes.exercise eq exe) }.firstOrNull() ?:
                                        throw NotFoundException()
                                    ).dto()
                                )
                            }
                        }
                        post {
                            call.authorizeAPI { user ->
                                val exercise = Exercise[call.parameters.getOrFail<Int>(ID.exercise)]
                                val code = call.receiveText()

                                call.respond(checkCode(user, exercise, code))
                            }
                        }
                    }
                }
                route(PATH.achievements) {
                    get {
                        call.authorizeAPI { user ->
                            call.respond(user.achievements.map { it.dto() })
                        }
                    }
                }
            }
        }
        route(PATH.exercises) {
            get {
                newSuspendedTransaction {
                    call.respond(Exercise.all().map { it.dto() })
                }
            }
        }
        route("${PATH.code}/{${ID.exercise}}") {
            post {
                newSuspendedTransaction {
                    val user = call.authentication.principal<User>()
                    val exercise = Exercise[call.parameters.getOrFail<Int>(ID.exercise)]
                    val code = call.receiveText()

                    call.respond(checkCode(user, exercise, code))
                }
            }
        }
    }
}