import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

    fun eval(code: String): Any? = engine.eval(code, bindings)

    fun server(args: Array<String>) {
        engine.eval("fun main(){}", bindings)

        embeddedServer(Netty, args.getOrNull(0)?.toIntOrNull() ?: 8080) {
            routing {
                static("/") {
                    resources("static")
                }
                get("/") {
                    call.respondRedirect("/index.html")
                }
                post("/") {
                    val param = call.receiveParameters()
                    val code = param["code"]
                    if (code != null) {
                        bindings.clear()
                        os.reset()
                        System.setOut(ps)

                        try {
                            engine.eval(code, bindings)

                            call.respondText {
                                when (param["exercise"]?.toIntOrNull()) {
                                    1 -> exe1()
                                    2 -> exe2()
                                    3 -> exe3()
                                    4 -> exe4()
                                    5 -> exe5()
                                    6 -> exe6()
                                    7 -> exe7()
                                    else -> "Не верно указан номер задачи"
                                }
                            }
                        } catch (ex: Exception) {
                            call.respondText { "В коде есть ошибки.\n${ex.message}" }
                        }

                        System.setOut(console)
                    } else {
                        call.respondText { "Отсутствует исходный код по задаче" }
                    }
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

    @JvmStatic
    fun main(args: Array<String>) {
        server(args)
    }
}
