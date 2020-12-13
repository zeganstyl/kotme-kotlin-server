package org.thelemistix.kotme

import org.json.simple.JSONObject
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.script.ScriptEngineManager

@Service
class ScriptService {
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")!!

    val os = ByteArrayOutputStream()
    val ps = PrintStream(os)
    val console = System.out

    val bindings = engine.createBindings()

    init {
        engine.eval("fun main(){}", bindings)
    }

    fun checkRunning(): String = "KOTme is running"

    fun checkExercise(param: Map<String, String>): String {
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

        return json.toJSONString()
    }
}