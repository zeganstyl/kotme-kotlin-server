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
import io.ktor.server.netty.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    val config = HikariConfig().apply {
        if (testing) {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test"
        } else {
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
        }

        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    Database.connect(HikariDataSource(config))

    transaction {
        SchemaUtils.create(*Main.tables)

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
        if (Achievement.count() < 5) {
            Achievement.new("Познакомься с Котлином", "Выполните первое задание")
            Achievement.new("Дальше-интереснее", "Выполните 2 задание")
            Achievement.new("На половине пути", "Выполните 5 заданий")
            Achievement.new("Почти у цели", "Выполните 9 задание")
            Achievement.new("Искатель сокровищ", "Выполните все задания")
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
                authenticate(credentials.name, credentials.password)
            }
        }
        jwt("jwt") {
            verifier(JwtConfig.verifier)
            realm = "kotme.com"
            validate {
                val id = it.payload.getClaim("id")?.asString()?.toIntOrNull()
                if (id != null) User.findById(id) else null
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

        static("/static") {
            resources("static")
        }
        static("docs") {
            resources("docs")
        }
    }
}

object Main {
    val host = BasicJvmScriptingHost()
    val compilationConfiguration = ScriptCompilationConfiguration()

    val console = System.out

    val tables = arrayOf(
        Achievements,
        Exercises,
        UserAchievements,
        UserCodes,
        Users
    )

    fun eval(code: String): ResultWithDiagnostics<EvaluationResult> {
        return host.eval(SourceCodeImp(code), compilationConfiguration, null)
    }
}
