package com.thelemistix.kotme

import org.json.simple.JSONObject
import org.springframework.stereotype.Service
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics

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
                val result: EvalResult? = when (param["exercise"]?.toIntOrNull()) {
                    1 -> exe1(code)
                    2 -> exe2(code)
                    3 -> exe3(code)
                    4 -> exe4(code)
                    5 -> exe5(code)
                    6 -> exe6(code)
                    7 -> exe7(code)
                    8 -> exe8(code)
                    9 -> exe9(code)
                    10 -> exe10(code)
                    else -> {
                        status = ResultStatus.IncorrectInput
                        message = "Не верно указан номер задачи"
                        null
                    }
                }

                System.setOut(Main.console)

                when (result) {
                    is ResultWithDiagnostics.Failure -> {
                        status = ResultStatus.TestsFail
                        val str = StringBuilder()
                        str.append("Ошибки компиляции кода\n")
                        result.reports.forEach {
                            str.append(it.render())
                            str.append('\n')
                        }
                        message = str.toString()
                    }
                    is ResultWithDiagnostics.Success -> {
                        status = ResultStatus.TestsSuccess
                        val returnValue = result.value.returnValue
                        if (returnValue is ResultValue.Error) {
                            status = ResultStatus.TestsFail
                            message = "Ошибки выполнения кода\n"
                            message += returnValue.error.message
                        }
                    }
                }

                consoleLog = Main.os.toString()

                if (message.isNotEmpty() && status == ResultStatus.TestsSuccess) {
                    status = ResultStatus.TestsFail
                }
            } catch (ex: Exception) {
                status = ResultStatus.ServerError
                consoleLog = Main.os.toString()
                message = "Ошибка сервера"
            }

            System.setOut(Main.console)
        } else {
            status = ResultStatus.IncorrectInput
            message = "Отсутствует исходный код по задаче"
        }

        val json = JSONObject()
        json["status"] = status
        json["message"] = message
        json["console"] = consoleLog

        return json.toJSONString()
    }
}