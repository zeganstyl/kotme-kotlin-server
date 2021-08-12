package com.kotme.routes

import com.kotme.EvalResult
import com.kotme.Main
import com.kotme.model.CodeCheckResultStatus
import com.kotme.*
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

fun authenticate(login: String, password: String): User? {
    if (login.isEmpty()) return null
    val u = User.find { Users.login eq login }.firstOrNull()
    return if (u != null && u.password.isNotEmpty() && BCrypt.checkpw(password, u.password)) u else null
}

suspend fun ApplicationCall.authorizeAPI(accessGranted: User.() -> Boolean, block: suspend (user: User) -> Unit) {
    newSuspendedTransaction {
        val user = authentication.principal<User>() ?: throw UnauthorizedException()
        if (accessGranted(user)) block(user) else throw ForbiddenException()
    }
}

suspend fun ApplicationCall.authorizeAPI(accessGranted: Boolean, block: suspend (user: User) -> Unit) {
    newSuspendedTransaction {
        val user = authentication.principal<User>() ?: throw UnauthorizedException()
        if (accessGranted) block(user) else throw ForbiddenException()
    }
}

suspend fun ApplicationCall.authorizeAPI(block: suspend (user: User) -> Unit) {
    val user = authentication.principal<User>() ?: throw UnauthorizedException()
    newSuspendedTransaction { block(user) }
}

fun Routing.apiRoutes() {
    route("/api") {
        authenticate("basic") {
            route("/token") {
                get {
                    call.authorizeAPI {
                        call.respond(HttpStatusCode.OK, JwtConfig.makeToken(it))
                    }
                }
            }
        }
        route("/signup") {
            post {
                newSuspendedTransaction {
                    val name = call.parameters.getOrFail<String>("name")
                    if (name.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, "Name is empty")
                    }

                    val login = call.parameters.getOrFail<String>("login")
                    if (Users.select { Users.login eq login }.count() > 0) {
                        call.respond(HttpStatusCode.BadRequest, "Login already exists")
                    }

                    val password = call.parameters.getOrFail<String>("password")
                    if (password.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, "Incorrect password")
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
        route("/achievements") {
            get {
                newSuspendedTransaction {
                    call.respond(Achievement.all().map { AchievementDTO(it) })
                }
            }
        }
        authenticate("jwt") {
            route("/user") {
                route("/updates/{from}") {
                    get {
                        call.authorizeAPI { user ->
                            val from = call.parameters.getOrFail<Long>("from")
                            call.respond(
                                UpdatesDTO(
                                    UserDTO(user, from),
                                    Exercise.find { Exercises.lastModifiedTime greater from }.map { ExerciseDTO(it) },
                                    Achievement.find { Achievements.lastModifiedTime greater from }.map { AchievementDTO(it) }
                                )
                            )
                        }
                    }
                }
                route("/codes") {
                    get {
                        call.authorizeAPI { user ->
                            call.respond(user.codes.map { UserCodeDTO(it) })
                        }
                    }
                    route("/{exercise}") {
                        get {
                            call.authorizeAPI { user ->
                                val exe = call.int("exercise")
                                call.respond(
                                    UserCodeDTO(
                                        UserCode.wrapRow(
                                            UserCodes.select { (UserCodes.user eq user.id) and (UserCodes.exercise eq exe) }.firstOrNull() ?:
                                            throw NotFoundException()
                                        )
                                    )
                                )
                            }
                        }
                        post {
                            call.authorizeAPI { user ->
                                var message = ""
                                var status = CodeCheckResultStatus.TestsSuccess
                                var consoleLog: String

                                val code = call.receiveText()
                                val exercise = Exercise[call.parameters.getOrFail<Int>("exercise")]

                                val row = UserCodes.select { (UserCodes.user eq user.id.value) and (UserCodes.exercise eq exercise.id.value) }.firstOrNull()
                                val userCode = if (row != null) {
                                    UserCode.wrapRow(row).apply {
                                        this.code = code
                                    }
                                } else {
                                    UserCode.new {
                                        this.exercise = exercise
                                        this.user = user
                                        this.code = code
                                    }
                                }

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
                                            status = CodeCheckResultStatus.TestsFail
                                            val str = StringBuilder()
                                            str.append("Ошибки компиляции кода\n")
                                            result.reports.forEach {
                                                str.append(it.render())
                                                str.append('\n')
                                            }
                                            message = str.toString()
                                        }
                                        is ResultWithDiagnostics.Success -> {
                                            status = CodeCheckResultStatus.TestsSuccess
                                            val returnValue = result.value.returnValue
                                            if (returnValue is ResultValue.Error) {
                                                status = CodeCheckResultStatus.TestsFail
                                                message = "Ошибки выполнения кода\n"
                                                message += returnValue.error.message
                                            }
                                        }
                                    }

                                    consoleLog = os.toString()

                                    if (message.isNotEmpty() && status == CodeCheckResultStatus.TestsSuccess) {
                                        status = CodeCheckResultStatus.TestsFail
                                    }
                                } catch (ex: Exception) {
                                    status = CodeCheckResultStatus.ServerError
                                    consoleLog = os.toString()
                                    message = "Ошибка сервера"
                                }

                                System.setOut(Main.console)

                                // new achievements ====
                                val newAchievements = ArrayList<UserAchievementDTO>()
                                if (status == CodeCheckResultStatus.TestsSuccess) {
                                    if (userCode.completeTime == 0L) {
                                        userCode.completeTime = System.currentTimeMillis()
                                    }

                                    val achivs = user.achievements
                                    if (exercise.number == 1) {
                                        if (achivs.find { it.achievement.id.value == 1 } == null) {
                                            newAchievements.add(UserAchievementDTO(UserAchievement.new(user, Achievement[1])))
                                        }
                                    }
                                    if (achivs.find { it.achievement.id.value == 2 } == null) {
                                        if (user.codes.count { it.completeTime != 0L } >= 5) {
                                            newAchievements.add(UserAchievementDTO(UserAchievement.new(user, Achievement[2])))
                                        }
                                    }
                                    if (achivs.find { it.achievement.id.value == 3 } == null) {
                                        if (user.codes.count { it.completeTime != 0L } == 10) {
                                            newAchievements.add(UserAchievementDTO(UserAchievement.new(user, Achievement[3])))
                                        }
                                    }
                                }
                                // ====

                                call.respond(CodeCheckResult(status, message, consoleLog, newAchievements))
                            }
                        }
                    }
                }
                route("/achievements") {
                    get {
                        call.authorizeAPI { user ->
                            call.respond(user.achievements.map { UserAchievementDTO(it) })
                        }
                    }
                }
            }
        }
        route("/exercises") {
            get {
                newSuspendedTransaction {
                    call.respond(Exercise.all().map { ExerciseDTO(it) })
                }
            }
        }
    }
}