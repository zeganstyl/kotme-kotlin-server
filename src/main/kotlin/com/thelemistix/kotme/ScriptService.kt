package com.thelemistix.kotme

import org.json.simple.JSONObject
import org.springframework.stereotype.Service

@Service
class ScriptService {
    fun checkRunning(): String = "KOTme is running"

    fun checkExercise(param: Map<String, String>): String {
        val code = param["code"]

        var message = ""
        var status = ResultStatus.TestsSuccess
        var consoleLog = ""

        if (code != null) {
            Main.os.reset()
            System.setOut(Main.ps)

            try {
                message = when (param["exercise"]?.toIntOrNull()) {
//                    1 -> exe1(code)
//                    2 -> exe2(code)
//                    3 -> exe3(code)
                    //4 -> exe4()
//                    5 -> exe5()
//                    6 -> exe6()
//                    7 -> exe7()
//                    8 -> exe8()
//                    9 -> exe9()
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