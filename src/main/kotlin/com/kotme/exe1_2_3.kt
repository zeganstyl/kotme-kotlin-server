package com.kotme

import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics

typealias EvalResult = ResultWithDiagnostics<EvaluationResult>
typealias ErrorResult = ResultWithDiagnostics.Failure

fun rand() = Random.nextInt(0, Int.MAX_VALUE)

fun error(text: String): ResultWithDiagnostics<EvaluationResult> = ResultWithDiagnostics.Success(
    value = EvaluationResult(ResultValue.Error(Exception(text)), null)
)

// Ready
fun exe1(code: String, os: ByteArrayOutputStream): EvalResult? {
    val result = Main.eval(code + """
main()
""")

    if (result is ErrorResult) return result

    val text = os.toString().trim()
    return if (text != "Hello Kotlin!") {
        error("Ваш код выдал:\n$text\nНо это не верно.")
    } else null
}

// Ready
fun exe2(code: String): EvalResult = Main.eval(code + """
for (i in 0 until 10) {
    val x1 = kotlin.random.Random.nextFloat() * 100f - 50f
    val y1 = kotlin.random.Random.nextFloat() * 100f - 50f
    val x2 = kotlin.random.Random.nextFloat() * 100f - 50f
    val y2 = kotlin.random.Random.nextFloat() * 100f - 50f

    val dist = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    
    val distanceResult = distance(x1, y1, x2, y2)
    if (distanceResult != dist) {
        throw Exception("Тест не пройден:\ndistance(${'$'}x1, ${'$'}y1, ${'$'}x2, ${'$'}y2)\nВаш код выдал: ${'$'}distanceResult\nНо ожидалось: ${'$'}dist")
    }
}
"""
)

// Ready
fun exe3(code: String, os: ByteArrayOutputStream): EvalResult {
    for (i in 0 until 3) {
        os.reset()

        val steps = Random.nextInt(5, 30)

        var stepsStr = ""

        for (j in 0 until steps) {
            stepsStr += if (j == steps - 1) "Шаг ${j+1} последний\n" else "Шаг ${j+1} идем далее\n"
        }

        val result1 = Main.eval(code + """
stepsCounting($steps)
""")
        if (result1 is ErrorResult) return result1

        val text = os.toString().trim()
        if (text != stepsStr.trim()) {
            return error("""
Тест не пройден:
stepsCounting($steps)
Ваш код выдал:
$text
Но это не верно
Ожидалось:
$stepsStr
""")
        }
    }

    val rand = Random.nextInt(5, 30)
    val steps2 = Random.nextInt(5, 30)
    val result = Main.eval(code + """
var result$rand: Int = -1
moveToGoal { step ->
    result$rand = step
    step == $steps2
}
if (result$rand != $steps2) {
    throw Exception("Ваш код в moveToGoal не срабатывает на нужном шаге")
}
""")

    return result
}
