package com.thelemistix.kotme

import org.json.simple.JSONObject
import org.springframework.stereotype.Service

@Service
class ScriptService {
    init {
        Main.initiate()
    }

    fun checkRunning(): String = "KOTme is running"

    fun checkExercise(param: Map<String, String>): String {
        val code = param["code"]

        var message = ""
        var status = ResultStatus.TestsSuccess
        var consoleLog = ""

        if (code != null) {
            Main.bindings.clear()
            Main.os.reset()
            System.setOut(Main.ps)

            try {
                Main.engine.eval(code, Main.bindings)

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

                consoleLog = Main.os.toString()

                if (message.isNotEmpty() && status == ResultStatus.TestsSuccess) {
                    status = ResultStatus.TestsFail
                }
            } catch (ex: Exception) {
                status = ResultStatus.ExecutionErrors
                consoleLog = Main.os.toString()
                message = "В коде есть ошибки.\n${ex.message}"
            }

            System.setOut(Main.console)
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