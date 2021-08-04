package com.kotme

import com.kotme.model.*
import com.kotme.routes.apiRoutes
import com.kotme.routes.ForbiddenException
import com.kotme.routes.UnauthorizedException
import com.kotme.routes.authenticate
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Main {
    val host = BasicJvmScriptingHost()
    val compilationConfiguration = ScriptCompilationConfiguration {
        this.dependencies
    }

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)
    val console = System.out

    @JvmStatic
    fun main(args: Array<String>) {
        val port = System.getenv("PORT")?.toIntOrNull() ?: 8888
        embeddedServer(Netty, port = port) {
            val config = HikariConfig().apply {
                val envDbUrl = System.getenv("DATABASE_URL")

                if (envDbUrl != null) {
                    val dbUri = URI(envDbUrl)
                    username = dbUri.userInfo.split(":")[0]
                    password = dbUri.userInfo.split(":")[1]
                    jdbcUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
                    driverClassName = "org.postgresql.Driver"
                } else {
                    driverClassName = "org.h2.Driver"
                    jdbcUrl = "jdbc:h2:file:./com.kotme.db-test"
                    //jdbcUrl = "jdbc:h2:mem:test"
                }

                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }

            Database.connect(HikariDataSource(config))

            transaction {
                SchemaUtils.create(
                    Achievements,
                    Exercises,
                    UserAchievements,
                    UserCodes,
                    Users
                )

                if (Exercise.count() < 10) {
                    Exercise.new(1, "Привет Kotlin")
                    Exercise.new(2, "Функции и примитивные типы")
                    Exercise.new(3, "Ветвления и циклы")
                    Exercise.new(4, "Массивы и коллекции")
                    Exercise.new(5, "Ассоциативные массивы")
                    Exercise.new(6, "Классы")
                    Exercise.new(7, "Свойства и переопределение")
                    Exercise.new(8, "Наследование")
                    Exercise.new(9, "Обобщения")
                    Exercise.new(10, "DSL")
                }
                if (Achievement.count() < 3) {
                    Achievement.new("Познакомься с Котлином", "Выполните первое задание")
                    Achievement.new("Подержи мою шляпу", "Выполните 5 заданий")
                    Achievement.new("Искатель приключений", "Выполните все задания")
                }
            }

            install(StatusPages) {
                exception<BadRequestException> {
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                }
                exception<SerializationException> {
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                }
                exception<EntityNotFoundException> {
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                }
                exception<ForbiddenException> {
                    call.respond(HttpStatusCode.Forbidden, it.message ?: "")
                }
                exception<UnauthorizedException> {
                    call.respond(HttpStatusCode.Unauthorized, it.message ?: "")
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    encodeDefaults = true
                    isLenient = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                    prettyPrint = false
                    useArrayPolymorphism = true
                    ignoreUnknownKeys = true
                })
            }

            install(Authentication) {
                basic("basic") {
                    realm = "kotme.com"
                    validate { credentials ->
                        newSuspendedTransaction {
                            authenticate(credentials.name, credentials.password)
                        }
                    }
                }
                jwt("jwt") {
                    verifier(JwtConfig.verifier)
                    realm = "kotme.com"
                    validate {
                        val id = it.payload.getClaim("id")?.asString()?.toIntOrNull()
                        if (id != null) transaction { User.findById(id) } else null
                    }
                }
            }

            routing {
                get("/") {
                    call.respondText("KOTme is running")
                }
                get("/docs") {
                    call.respondRedirect("/docs/index.html")
                }
                apiRoutes()
//                post {
//                    val param = call.receiveParameters()
//                    val code = param["code"]
//
//                    var message = ""
//                    var status = ResultStatus.TestsSuccess
//                    var consoleLog = ""
//
//                    if (code != null) {
//                        os.reset()
//                        System.setOut(ps)
//
//                        try {
//                            val result: EvalResult? = when (param["exercise"]?.toIntOrNull()) {
//                                1 -> exe1(code)
//                                2 -> exe2(code)
//                                3 -> exe3(code)
//                                4 -> exe4(code)
//                                5 -> exe5(code)
//                                6 -> exe6(code)
//                                7 -> exe7(code)
//                                8 -> exe8(code)
//                                9 -> exe9(code)
//                                10 -> exe10(code)
//                                else -> {
//                                    status = ResultStatus.IncorrectInput
//                                    message = "Не верно указан номер задачи"
//                                    null
//                                }
//                            }
//
//                            System.setOut(console)
//
//                            when (result) {
//                                is ResultWithDiagnostics.Failure -> {
//                                    status = ResultStatus.TestsFail
//                                    val str = StringBuilder()
//                                    str.append("Ошибки компиляции кода\n")
//                                    result.reports.forEach {
//                                        str.append(it.render())
//                                        str.append('\n')
//                                    }
//                                    message = str.toString()
//                                }
//                                is ResultWithDiagnostics.Success -> {
//                                    status = ResultStatus.TestsSuccess
//                                    val returnValue = result.value.returnValue
//                                    if (returnValue is ResultValue.Error) {
//                                        status = ResultStatus.TestsFail
//                                        message = "Ошибки выполнения кода\n"
//                                        message += returnValue.error.message
//                                    }
//                                }
//                            }
//
//                            consoleLog = os.toString()
//
//                            if (message.isNotEmpty() && status == ResultStatus.TestsSuccess) {
//                                status = ResultStatus.TestsFail
//                            }
//                        } catch (ex: Exception) {
//                            status = ResultStatus.ServerError
//                            consoleLog = os.toString()
//                            message = "Ошибка сервера"
//                        }
//
//                        System.setOut(console)
//                    } else {
//                        status = ResultStatus.IncorrectInput
//                        message = "Отсутствует исходный код по задаче"
//                    }
//
//                    call.respond(ResultMessage(status, message, consoleLog))
//                }

                static("/static") {
                    resources("static")
                }
                static("docs") {
                    resources("docs")
                }
            }
        }.start(true)
    }

    fun eval(code: String): ResultWithDiagnostics<EvaluationResult> {
        return host.eval(SourceCodeImp(code), compilationConfiguration, null)
    }
}
