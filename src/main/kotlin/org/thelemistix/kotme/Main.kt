package org.thelemistix.kotme

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.json.simple.JSONObject
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.script.ScriptEngineManager

object Main {
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")!!

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)
    val console = System.out

    val bindings = engine.createBindings()

    @JvmStatic
    fun main(args: Array<String>) {
        server(args)
    }

    fun eval(code: String): Any? = engine.eval(code, bindings)

    fun server(args: Array<String>) {
        engine.eval("fun main(){}", bindings)

        embeddedServer(
            Netty,
            port = args.getOrNull(0)?.toIntOrNull() ?: System.getenv()["PORT"]?.toIntOrNull() ?: 8888,
            host = "localhost"
        ) {
            routing {
                get("/") {
                    call.respondText { "KOTme is running" }
                }
                post("/") {
                    val param = call.receiveParameters()
                    val code = param["code"]

                    var message = ""
                    var status = ResultStatus.TestsSuccess
                    var consoleLog = ""

                    if (code != null) {
                        bindings.clear()
                        os.reset()
                        System.setOut(ps)

                        try {
                            engine.eval(code, bindings)

                            message = when (param["exercise"]?.toIntOrNull()) {
                                1 -> exe1()
                                2 -> exe2()
                                3 -> exe3()
                                4 -> exe4()
                                5 -> exe5()
                                6 -> exe6()
                                7 -> exe7()
                                else -> {
                                    status = ResultStatus.ServerError
                                    "Не верно указан номер задачи"
                                }
                            }

                            consoleLog = os.toString()

                            if (message.isNotEmpty() && status == ResultStatus.TestsSuccess) {
                                status = ResultStatus.TestsFail
                            }
                        } catch (ex: Exception) {
                            status = ResultStatus.ExecutionErrors
                            consoleLog = os.toString()
                            message = "В коде есть ошибки.\n${ex.message}"
                        }

                        System.setOut(console)
                    } else {
                        status = ResultStatus.ServerError
                        message = "Отсутствует исходный код по задаче"
                    }

                    val json = JSONObject()
                    json["status"] = status
                    json["message"] = message
                    json["console"] = consoleLog
                    call.respondText { json.toJSONString() }
                }
            }
        }.start(wait = true)
    }

    fun script() {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")!!

        val os = ByteArrayOutputStream()
        val ps = PrintStream(os)
        val console = System.out

        val bindings = engine.createBindings()
        bindings.clear()

        System.setOut(ps)
        engine.eval("""
fun stepsCounting() {}

fun moveToGoal() {}
        """.trimIndent(), bindings)
        engine.eval("moveToGoal()", bindings)
        System.setOut(console)

        println(os.toString())
    }
}
